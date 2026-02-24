package com.ecom.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.LoginLogService;
import com.ecom.service.OtpService;
import com.ecom.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class OtpController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserService userService;

    @Autowired
    private LoginLogService loginLogService;

    // ─── Login OTP ───────────────────────────────────────────────────────────

    @GetMapping("/verify-otp")
    public String showOtpPage(HttpSession session) {
        // ถ้าไม่มี otpUser ใน session ให้กลับไปหน้า login
        if (session.getAttribute("otpUser") == null) {
            return "redirect:/signin";
        }
        return "verify_otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("otpUser");

        if (email == null) return "redirect:/signin";

        UserDtls user = userRepository.findByEmail(email);

        if (otpService.validateOtp(user, otp)) {

            session.removeAttribute("otpUser");
            loginLogService.saveLoginLog(user);

            if (user.getRole().equals("ROLE_ADMIN"))
                return "redirect:/admin/";
            else
                return "redirect:/";

        } else {
            redirectAttributes.addFlashAttribute("error", "รหัส OTP ไม่ถูกต้องหรือหมดอายุแล้ว");
            return "redirect:/verify-otp";
        }
    }

    // ─── Registration OTP ─────────────────────────────────────────────────────

    @GetMapping("/verify-register-otp")
    public String showRegisterOtpPage() {
        // ไม่มีหน้าแยกแล้ว — OTP อยู่บนหน้า register เดียวกัน
        return "redirect:/register";
    }

    @PostMapping("/verify-register-otp")
    public String verifyRegisterOtp(@RequestParam String otp,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        String storedOtp = (String) session.getAttribute("registerOtp");
        LocalDateTime expiry = (LocalDateTime) session.getAttribute("registerOtpExpiry");
        UserDtls pendingUser = (UserDtls) session.getAttribute("pendingUser");

        if (storedOtp == null || pendingUser == null) {
            session.setAttribute("errorMsg", "Session หมดอายุ กรุณาสมัครใหม่");
            return "redirect:/register";
        }

        if (expiry == null || expiry.isBefore(LocalDateTime.now())) {
            clearRegisterSession(session);
            session.setAttribute("errorMsg", "OTP หมดอายุแล้ว กรุณาสมัครใหม่");
            return "redirect:/register";
        }

        if (!storedOtp.equals(otp)) {
            // ใช้ flash attribute เพื่อแสดงใน OTP section ของหน้า register
            redirectAttributes.addFlashAttribute("errorOtp", "รหัส OTP ไม่ถูกต้อง กรุณาลองใหม่");
            return "redirect:/register";
        }

        // OTP ถูกต้อง → บันทึก user
        pendingUser.setProfileImage("default.png");
        UserDtls savedUser = userService.saveUser(pendingUser);

        clearRegisterSession(session);

        if (savedUser != null) {
            session.setAttribute("succMsg", "สมัครสมาชิกสำเร็จ! กรุณาล็อกอิน");
        } else {
            session.setAttribute("errorMsg", "เกิดข้อผิดพลาด กรุณาลองใหม่");
        }

        return "redirect:/register";
    }

    private void clearRegisterSession(HttpSession session) {
        session.removeAttribute("pendingUser");
        session.removeAttribute("registerOtp");
        session.removeAttribute("registerOtpExpiry");
    }
}

