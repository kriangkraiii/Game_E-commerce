package com.ecom.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Transaction;
import com.ecom.model.UserDtls;
import com.ecom.model.Wallet;
import com.ecom.model.WalletTransfer;
import com.ecom.repository.TransactionRepository;
import com.ecom.repository.UserRepository;
import com.ecom.repository.WalletRepository;
import com.ecom.repository.WalletTransferRepository;
import com.ecom.service.EasySlipService.SlipValidationResult;

/**
 * Service หลักสำหรับ Digital Wallet
 * จัดการ Wallet, Transaction และ Top-up flow ทั้งหมด
 */
@Service
public class WalletService {

    private static final String SLIP_UPLOAD_DIR = "uploads/slips/";

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EasySlipService easySlipService;

    @Autowired
    private PromptPayService promptPayService;

    @Autowired
    private WalletTransferRepository walletTransferRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== WALLET ====================

    /**
     * ดึง Wallet ของ User (สร้างใหม่ถ้ายังไม่มี)
     */
    public Wallet getOrCreateWallet(UserDtls user) {
        return walletRepository.findByUser(user).orElseGet(() -> {
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setTotalTopup(BigDecimal.ZERO);
            return walletRepository.save(wallet);
        });
    }

    /**
     * ดึงยอดคงเหลือ
     */
    public BigDecimal getBalance(UserDtls user) {
        Wallet wallet = getOrCreateWallet(user);
        return wallet.getBalance();
    }

    // ==================== QR CODE ====================

    /**
     * สร้าง URL สำหรับ QR Code รูปภาพ (.png) จาก promptpay.io
     */
    public String generateQrImageUrl(BigDecimal amount) {
        return promptPayService.generateQrImageUrl(amount);
    }

    /**
     * สร้าง URL สำหรับหน้า promptpay.io
     */
    public String generatePromptPayPageUrl(BigDecimal amount) {
        return promptPayService.generatePromptPayPageUrl(amount);
    }

    // ==================== TOP UP ====================

    /**
     * ขั้นตอนเต็ม: ตรวจสอบสลิปและเติมเงิน
     * 
     * Flow:
     * 1. บันทึก Transaction (status = PENDING)
     * 2. บันทึกไฟล์สลิป
     * 3. ส่งสลิปไปตรวจสอบที่ EasySlip API
     * 4. ถ้าผ่าน → อัพเดท Wallet balance + Transaction status = SUCCESS
     * 5. ถ้าไม่ผ่าน → Transaction status = FAILED พร้อมเหตุผล
     */
    @Transactional
    public TopUpResult processTopUp(UserDtls user, MultipartFile slipFile, BigDecimal amount, Instant qrTimestamp) {
        // 1. สร้าง Transaction (PENDING)
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setStatus(Transaction.Status.PENDING);
        transaction = transactionRepository.save(transaction);

        try {
            // 2. บันทึกไฟล์สลิป
            String slipPath = saveSlipFile(slipFile, transaction.getId());
            transaction.setSlipImagePath(slipPath);
            transactionRepository.save(transaction);

            // 3. ส่งสลิปไปตรวจสอบ
            SlipValidationResult result = easySlipService.validateSlip(slipFile, amount.doubleValue());

            if (!result.isValid()) {
                // ไม่ผ่าน
                transaction.setStatus(Transaction.Status.FAILED);
                transaction.setFailureReason(result.getErrorMessage());
                transactionRepository.save(transaction);
                return TopUpResult.failed(transaction, result.getErrorMessage());
            }

            // Time Validation
            String transDateStr = result.getSlipData().getTransDate();
            if (qrTimestamp != null && transDateStr != null && !transDateStr.isEmpty()) {
                try {
                    String normalizedDate = transDateStr.replace(" ", "T");
                    if (!normalizedDate.contains("+") && !normalizedDate.contains("Z")) {
                        normalizedDate += "+07:00"; // Assume Thailand time if no offset
                    }
                    Instant slipTime = OffsetDateTime.parse(normalizedDate).toInstant();

                    if (slipTime.isBefore(qrTimestamp.minusSeconds(120))) {
                        transaction.setStatus(Transaction.Status.FAILED);
                        transaction.setFailureReason("เวลาในสลิปเกิดก่อนการสร้าง QR Code");
                        transactionRepository.save(transaction);
                        return TopUpResult.failed(transaction, "เวลาในสลิปไม่สอดคล้อง (เกิดก่อนสร้าง QR)");
                    }

                    if (slipTime.isAfter(qrTimestamp.plusSeconds(300))) {
                        transaction.setStatus(Transaction.Status.FAILED);
                        transaction.setFailureReason("เวลาในสลิปเกิน 5 นาทีหลังสร้าง QR Code");
                        transactionRepository.save(transaction);
                        return TopUpResult.failed(transaction, "สลิปหมดอายุ (เกิน 5 นาที)");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 4. ตรวจสอบว่าสลิปซ้ำหรือไม่ (ป้องกัน double spending)
            String transRef = result.getSlipData().getTransRef();
            if (transRef != null && !transRef.isEmpty()
                    && transactionRepository.existsByRefTransactionId(transRef)) {
                transaction.setStatus(Transaction.Status.FAILED);
                transaction.setFailureReason("สลิปนี้ถูกใช้งานแล้ว (transRef: " + transRef + ")");
                transactionRepository.save(transaction);
                return TopUpResult.failed(transaction, "สลิปนี้ถูกใช้งานแล้ว");
            }

            // 5. สำเร็จ — อัพเดท Transaction
            transaction.setStatus(Transaction.Status.SUCCESS);
            transaction.setRefTransactionId(transRef);
            transaction.setSenderName(result.getSlipData().getSenderName());
            transaction.setReceiverName(result.getSlipData().getReceiverName());
            transaction.setVerifiedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            // 6. อัพเดท Wallet
            Wallet wallet = getOrCreateWallet(user);
            wallet.setBalance(wallet.getBalance().add(amount));
            wallet.setTotalTopup(wallet.getTotalTopup().add(amount));
            wallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(wallet);

            return TopUpResult.success(transaction, wallet.getBalance());

        } catch (Exception e) {
            transaction.setStatus(Transaction.Status.FAILED);
            transaction.setFailureReason("เกิดข้อผิดพลาดภายใน: " + e.getMessage());
            transactionRepository.save(transaction);
            return TopUpResult.failed(transaction, "เกิดข้อผิดพลาดภายใน: " + e.getMessage());
        }
    }

    // ==================== TRANSACTION HISTORY ====================

    /**
     * ดึงประวัติการทำธุรกรรมของ User
     */
    public List<Transaction> getTransactionHistory(UserDtls user) {
        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * ดึงประวัติการทำธุรกรรมตาม status
     */
    public List<Transaction> getTransactionsByStatus(UserDtls user, Transaction.Status status) {
        return transactionRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status);
    }

    // ==================== PURCHASE PAYMENT ====================

    @Transactional
    public PurchaseResult purchaseWithWallet(UserDtls user, BigDecimal amount, String description) {
        Wallet wallet = getOrCreateWallet(user);

        if (wallet.getBalance().compareTo(amount) < 0) {
            return PurchaseResult.failed(
                    String.format("ยอดเงินไม่เพียงพอ (คงเหลือ: ฿%.2f, ต้องการ: ฿%.2f)", wallet.getBalance(), amount));
        }

        try {
            wallet.setBalance(wallet.getBalance().subtract(amount));
            wallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(wallet);

            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setAmount(amount);
            transaction.setStatus(Transaction.Status.SUCCESS);
            transaction.setType(Transaction.Type.PURCHASE);
            transaction.setDescription(description);
            transaction.setVerifiedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            return PurchaseResult.success(transaction, wallet.getBalance());
        } catch (Exception e) {
            return PurchaseResult.failed("เกิดข้อผิดพลาด: " + e.getMessage());
        }
    }

    // ==================== ADMIN QUERIES ====================

    public BigDecimal getTotalPurchaseRevenue() {
        return transactionRepository.getTotalPurchaseRevenue();
    }

    public Long getTotalPurchaseCount() {
        return transactionRepository.getTotalPurchaseCount();
    }

    public BigDecimal getTotalTopupAmount() {
        return transactionRepository.getTotalTopupAmount();
    }

    public List<Transaction> getAllTransactions(int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return transactionRepository.findAllByOrderByCreatedAtDesc(pageable).getContent();
    }

    public Page<Transaction> getAllTransactionsPaginated(int pageNo, int pageSize) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNo - 1,
                pageSize);
        return transactionRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // ==================== PURCHASE RESULT CLASS ====================

    public static class PurchaseResult {
        private boolean success;
        private String message;
        private Transaction transaction;
        private BigDecimal newBalance;

        public static PurchaseResult success(Transaction transaction, BigDecimal newBalance) {
            PurchaseResult r = new PurchaseResult();
            r.success = true;
            r.message = "ชำระเงินสำเร็จ";
            r.transaction = transaction;
            r.newBalance = newBalance;
            return r;
        }

        public static PurchaseResult failed(String reason) {
            PurchaseResult r = new PurchaseResult();
            r.success = false;
            r.message = reason;
            return r;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public BigDecimal getNewBalance() {
            return newBalance;
        }
    }

    // ==================== FILE HELPER ====================

    /**
     * บันทึกไฟล์สลิป
     */
    private String saveSlipFile(MultipartFile file, Long transactionId) throws IOException {
        Path uploadDir = Paths.get(SLIP_UPLOAD_DIR);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = "slip_" + transactionId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    // ==================== RESULT CLASS ====================

    /**
     * ผลลัพธ์การ Top-up
     */
    public static class TopUpResult {
        private boolean success;
        private String message;
        private Transaction transaction;
        private BigDecimal newBalance;

        public static TopUpResult success(Transaction transaction, BigDecimal newBalance) {
            TopUpResult r = new TopUpResult();
            r.success = true;
            r.message = "เติมเงินสำเร็จ";
            r.transaction = transaction;
            r.newBalance = newBalance;
            return r;
        }

        public static TopUpResult failed(Transaction transaction, String reason) {
            TopUpResult r = new TopUpResult();
            r.success = false;
            r.message = reason;
            r.transaction = transaction;
            return r;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public BigDecimal getNewBalance() {
            return newBalance;
        }
    }

    // ==================== WALLET TRANSFER ====================

    /**
     * โอนเงินจาก Wallet ของ sender ไปยัง receiver
     *
     * Flow:
     * 1. ตรวจสอบว่า receiver มีอยู่จริง
     * 2. ตรวจสอบว่าไม่โอนให้ตัวเอง
     * 3. ตรวจสอบยอดเงินเพียงพอ
     * 4. หักเงิน sender + เพิ่มเงิน receiver
     * 5. บันทึก WalletTransfer record
     */
    @Transactional
    public TransferResult transferToUser(UserDtls sender, String receiverEmail, BigDecimal amount, String note) {
        // 1. ตรวจสอบจำนวนเงิน
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return TransferResult.failed("จำนวนเงินต้องมากกว่า 0");
        }
        if (amount.compareTo(BigDecimal.valueOf(100000)) > 0) {
            return TransferResult.failed("โอนได้สูงสุด 100,000 บาทต่อครั้ง");
        }

        // 2. ค้นหาผู้รับจาก email
        UserDtls receiver = userRepository.findByEmail(receiverEmail);
        if (receiver == null) {
            return TransferResult.failed("ไม่พบผู้ใช้ที่มีอีเมล: " + receiverEmail);
        }

        // 3. ตรวจสอบว่าไม่โอนให้ตัวเอง
        if (sender.getId().equals(receiver.getId())) {
            return TransferResult.failed("ไม่สามารถโอนเงินให้ตัวเองได้");
        }

        // 3.5 ห้ามโอนเงินให้แอดมิน
        if ("ROLE_ADMIN".equals(receiver.getRole())) {
            return TransferResult.failed("ไม่สามารถโอนเงินให้แอดมินได้");
        }

        // 4. ตรวจสอบยอดเงินเพียงพอ
        Wallet senderWallet = getOrCreateWallet(sender);
        if (senderWallet.getBalance().compareTo(amount) < 0) {
            return TransferResult
                    .failed(String.format("ยอดเงินไม่เพียงพอ (คงเหลือ: ฿%.2f)", senderWallet.getBalance()));
        }

        try {
            // 5. หักเงินผู้ส่ง
            senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
            senderWallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(senderWallet);

            // 6. เพิ่มเงินผู้รับ
            Wallet receiverWallet = getOrCreateWallet(receiver);
            receiverWallet.setBalance(receiverWallet.getBalance().add(amount));
            receiverWallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(receiverWallet);

            // 7. บันทึกประวัติการโอน
            WalletTransfer transfer = new WalletTransfer();
            transfer.setSender(sender);
            transfer.setReceiver(receiver);
            transfer.setAmount(amount);
            transfer.setNote(note);
            transfer.setStatus(WalletTransfer.TransferStatus.SUCCESS);
            transfer.setSenderBalanceAfter(senderWallet.getBalance());
            transfer.setReceiverBalanceAfter(receiverWallet.getBalance());
            walletTransferRepository.save(transfer);

            return TransferResult.success(transfer, senderWallet.getBalance(), receiver.getName());

        } catch (Exception e) {
            // บันทึก transfer ที่ล้มเหลว
            WalletTransfer failedTransfer = new WalletTransfer();
            failedTransfer.setSender(sender);
            failedTransfer.setReceiver(receiver);
            failedTransfer.setAmount(amount);
            failedTransfer.setNote(note);
            failedTransfer.setStatus(WalletTransfer.TransferStatus.FAILED);
            failedTransfer.setFailureReason(e.getMessage());
            walletTransferRepository.save(failedTransfer);

            return TransferResult.failed("เกิดข้อผิดพลาดภายใน: " + e.getMessage());
        }
    }

    /**
     * ค้นหา User จากอีเมลเพื่อแสดงชื่อก่อนโอน
     */
    public UserDtls findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * ดึงประวัติการโอนทั้งหมดของ User (ทั้งส่งและรับ)
     */
    public List<WalletTransfer> getTransferHistory(UserDtls user) {
        return walletTransferRepository.findAllByUser(user);
    }

    /**
     * ดึงรายการที่เราโอนออก
     */
    public List<WalletTransfer> getSentTransfers(UserDtls user) {
        return walletTransferRepository.findBySenderOrderByCreatedAtDesc(user);
    }

    /**
     * ดึงรายการที่เราได้รับ
     */
    public List<WalletTransfer> getReceivedTransfers(UserDtls user) {
        return walletTransferRepository.findByReceiverOrderByCreatedAtDesc(user);
    }

    // ==================== TRANSFER RESULT CLASS ====================

    /**
     * ผลลัพธ์การโอนเงิน
     */
    public static class TransferResult {
        private boolean success;
        private String message;
        private WalletTransfer transfer;
        private BigDecimal newBalance;
        private String receiverName;

        public static TransferResult success(WalletTransfer transfer, BigDecimal newBalance, String receiverName) {
            TransferResult r = new TransferResult();
            r.success = true;
            r.message = "โอนเงินสำเร็จ";
            r.transfer = transfer;
            r.newBalance = newBalance;
            return r;
        }

        public static TransferResult failed(String reason) {
            TransferResult r = new TransferResult();
            r.success = false;
            r.message = reason;
            return r;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public WalletTransfer getTransfer() {
            return transfer;
        }

        public BigDecimal getNewBalance() {
            return newBalance;
        }

        public String getReceiverName() {
            return receiverName;
        }
    }
}
