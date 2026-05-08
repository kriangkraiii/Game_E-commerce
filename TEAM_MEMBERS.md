# 📋 รายชื่อสมาชิกและหน้าที่ความรับผิดชอบ — กลุ่ม G17

## 📌 ข้อมูลโปรเจกต์

| รายการ | รายละเอียด |
|--------|------------|
| **ชื่อโปรเจกต์** | Game E-Commerce — ระบบซื้อขายเกมออนไลน์ |
| **รายวิชา** | Web Programming |
| **กลุ่ม** | G17 |
| **จำนวนสมาชิก** | 5 คน |
| **เทคโนโลยีหลัก** | Spring Boot 4.0.3, Java 21, Thymeleaf, Spring Security, Spring WebSocket, MySQL, AWS S3 |
| **เทคโนโลยีเสริม** | ZXing QR Code, Zip4j (AES-256), Spring Mail, Hibernate Validator, H2 (Test) |

---

## 👥 รายชื่อสมาชิก

| ลำดับ | รหัสนักศึกษา | ชื่อ-นามสกุล | บทบาทหลัก |
|:-----:|:------------:|:-------------|:----------|
| 1 | 663380616-4 | นายเกรียงไกร ประเสริฐ | **Team Lead** / Full-Stack Developer |
| 2 | 663380587-5 | นายกิตติกร เสวกวิหารี | Backend Developer / Community Specialist |
| 3 | 663380594-8 | นางสาวตติยา บุตรเฟื้อย | Security Engineer / QA |
| 4 | 663380390-4 | นายปาณวัฒน์ จันทร์ทองหลาง | Frontend Developer / E-Commerce |
| 5 | 663380045-1 | นายปิติ มูลเทพพิชัย | Backend Developer / E-Commerce |

---

## 🔧 รายละเอียดหน้าที่ความรับผิดชอบแต่ละระบบ

### 1. 🛒 ระบบการซื้อขาย (E-Commerce System)

> ระบบหลักของแพลตฟอร์มสำหรับการซื้อขายเกมดิจิทัล ครอบคลุมตั้งแต่การจัดการสินค้าไปจนถึงกระบวนการชำระเงิน

**ผู้รับผิดชอบ:**
- **นายปาณวัฒน์ จันทร์ทองหลาง** (663380390-4)
- **นายเกรียงไกร ประเสริฐ** (663380616-4)
- **นายปิติ มูลเทพพิชัย** (663380045-1)

**ขอบเขตงานที่รับผิดชอบ:**

| โมดูล | รายละเอียด | ไฟล์หลักที่เกี่ยวข้อง |
|-------|------------|----------------------|
| **การจัดการสินค้า (Product Management)** | CRUD สินค้าเกม, จัดหมวดหมู่ (Category), อัปโหลดรูปภาพสินค้าไปยัง AWS S3, แสดงรายละเอียดสินค้า | `ProductService`, `CategoryService`, `FileService` |
| **ตะกร้าสินค้า (Shopping Cart)** | เพิ่ม/ลบสินค้าในตะกร้า, คำนวณราคารวม, จัดการจำนวนสินค้า | `CartService`, `Cart` model |
| **ระบบสั่งซื้อ (Order Management)** | สร้างคำสั่งซื้อ, ติดตามสถานะ, ประวัติการสั่งซื้อ, จัดการ Order Request | `OrderService`, `ProductOrder`, `OrderRequest` |
| **การชำระเงิน (Payment)** | รองรับ PromptPay QR Code (สร้างด้วย ZXing), ตรวจสอบสลิปผ่าน EasySlip API | `PromptPayService`, `EasySlipService` |
| **ส่งมอบสินค้าดิจิทัล (Secure Digital Delivery)** | จัดส่งไฟล์เกมแบบเข้ารหัส AES-256 ผ่าน Zip4j | `SecureDeliveryService` |
| **คลังเกม (Game Library)** | แสดงเกมที่ผู้ใช้ซื้อแล้ว, จัดการ Library ส่วนตัว | `GameLibraryService`, `GameLibrary` model |

**หน้าที่รับผิดชอบเฉพาะบุคคล:**

- **นายปาณวัฒน์** — ออกแบบและพัฒนาหน้า UI ฝั่งผู้ใช้งาน (Thymeleaf Templates): หน้าแสดงสินค้า, หน้าตะกร้าสินค้า, หน้าสั่งซื้อ, หน้ารายละเอียดสินค้า (`user/cart.html`, `user/order.html`, `guest/view_product.html`)
- **นายเกรียงไกร** — พัฒนา Backend API และ Business Logic: `ProductService`, `CartService`, `OrderService`, ระบบชำระเงิน PromptPay, การส่งมอบเกมดิจิทัลผ่าน `SecureDeliveryService`
- **นายปิติ** — พัฒนาระบบสั่งซื้อและตรวจสอบสลิป: `EasySlipService`, จัดการ Order lifecycle, เชื่อมต่อกับ `GameLibraryService`

---

### 2. 💬 ระบบชุมชน (Community System)

> ระบบสำหรับให้ผู้ใช้สร้างโพสต์ แสดงความคิดเห็น และมีปฏิสัมพันธ์กัน เปรียบเสมือนฟอรัมชุมชนเกมเมอร์

**ผู้รับผิดชอบ:**
- **นายกิตติกร เสวกวิหารี** (663380587-5)

**ขอบเขตงานที่รับผิดชอบ:**

| โมดูล | รายละเอียด | ไฟล์หลักที่เกี่ยวข้อง |
|-------|------------|----------------------|
| **การจัดการโพสต์ (Post Management)** | สร้าง/แก้ไข/ลบโพสต์, แสดงรายการโพสต์, ดูโพสต์ส่วนตัว | `CommunityPostService`, `Post` model |
| **ระบบแสดงความคิดเห็น (Comment System)** | แสดงความคิดเห็นในโพสต์, ลบความคิดเห็น | `CommunityService`, `Comment` model |
| **ระบบถูกใจ (Like System)** | กดถูกใจ/ยกเลิกถูกใจโพสต์ | `Like` model |
| **หน้าชุมชนผู้ใช้ (User Community Pages)** | หน้าชุมชนหลัก, หน้ารายละเอียดโพสต์, หน้าโพสต์ของฉัน, หน้าแก้ไขโพสต์ | `user/community.html`, `user/post_detail.html`, `user/my_posts.html` |
| **แอดมินจัดการชุมชน (Admin Community)** | แอดมินดูแลโพสต์ทั้งหมด, แก้ไข/ลบโพสต์ที่ไม่เหมาะสม, ดูความคิดเห็น | `AdminCommunityController`, `admin/community.html` |

**รายละเอียดงานเพิ่มเติม:**
- พัฒนา Controller ทั้ง 2 ตัว: `CommunityController` (ฝั่งผู้ใช้) และ `AdminCommunityController` (ฝั่งแอดมิน)
- ออกแบบ Data Model สำหรับ `Post`, `Comment`, `Like` พร้อมความสัมพันธ์ระหว่าง Entity
- สร้างหน้า Thymeleaf Templates ทั้งฝั่งผู้ใช้และแอดมิน รวม 6+ หน้า
- เขียน Service Layer: `CommunityService`, `CommunityPostService` สำหรับ Business Logic

---

### 3. 🔒 ระบบความปลอดภัย (Security & 2FA)

> ระบบรักษาความปลอดภัยของแพลตฟอร์ม ครอบคลุม Authentication, Authorization, การยืนยันตัวตนสองชั้น (2FA) และการป้องกัน Brute-Force Attack

**ผู้รับผิดชอบ:**
- **นางสาวตติยา บุตรเฟื้อย** (663380594-8)

**ขอบเขตงานที่รับผิดชอบ:**

| โมดูล | รายละเอียด | ไฟล์หลักที่เกี่ยวข้อง |
|-------|------------|----------------------|
| **ระบบสมัครสมาชิก (Registration)** | สมัครสมาชิกพร้อมตรวจสอบข้อมูล, ยืนยัน OTP ผ่านอีเมล | `UserService`, `OtpService` |
| **ระบบเข้าสู่ระบบ (Authentication)** | เข้าสู่ระบบด้วย Spring Security, จัดการ Session, Remember Me | `UserDetailsServiceImpl`, Security Config |
| **การยืนยันตัวตนสองชั้น (2FA/OTP)** | ส่ง OTP ผ่านอีเมล, ตรวจสอบ OTP, OTP สำหรับการสมัครและรีเซ็ตรหัสผ่าน | `OtpService`, `OtpController`, `EmailService` |
| **ลืมรหัสผ่าน (Forgot Password)** | ส่งลิงก์รีเซ็ตรหัสผ่านผ่านอีเมล, รีเซ็ตรหัสผ่านใหม่ | `guest/forgot_password.html`, `guest/reset_password.html` |
| **การล็อกบัญชี (Account Locking)** | ล็อกบัญชีอัตโนมัติหลังล็อกอินผิด 3 ครั้ง (ล็อก 15 นาที), แอดมินล็อก/ปลดล็อกบัญชีด้วยตนเอง | `UserService` |
| **บันทึกการเข้าสู่ระบบ (Login Logs)** | บันทึกทุกครั้งที่มีการล็อกอิน สำเร็จ/ล้มเหลว, IP Address, Timestamp | `LoginLogService`, `LoginLog` model |
| **ระบบบทบาท (Role-Based Access Control)** | แบ่งสิทธิ์ User/Admin, ป้องกันการเข้าถึงหน้าที่ไม่มีสิทธิ์ | Spring Security Config |

**รายละเอียดงานเพิ่มเติม:**
- กำหนดค่า Spring Security 6.x สำหรับ Authentication & Authorization
- พัฒนา `OtpController` สำหรับจัดการ OTP flow ทั้งหมด
- สร้างหน้า Thymeleaf: `verify_otp.html`, `verify_register_otp.html`, `guest/login.html`, `guest/register.html`
- เขียน Unit Test สำหรับระบบล็อกบัญชี (JUnit 5) ทดสอบ boundary conditions
- พัฒนาหน้าประวัติเข้าสู่ระบบ (`user/login_history.html`)

---

### 4. 💰 ระบบกระเป๋าเงินดิจิทัล (Digital Wallet)

> ระบบกระเป๋าเงินภายในแพลตฟอร์ม สำหรับเติมเงิน, ชำระเงิน และโอนเงินระหว่างผู้ใช้

**ผู้รับผิดชอบ:**
- **นายเกรียงไกร ประเสริฐ** (663380616-4)

**ขอบเขตงานที่รับผิดชอบ:**

| โมดูล | รายละเอียด | ไฟล์หลักที่เกี่ยวข้อง |
|-------|------------|----------------------|
| **กระเป๋าเงิน (Wallet)** | สร้าง Wallet อัตโนมัติเมื่อสมัครสมาชิก, แสดงยอดคงเหลือ, ดึงประวัติธุรกรรม | `WalletService`, `Wallet` model |
| **การเติมเงิน (Top-up)** | เติมเงินผ่าน PromptPay QR Code, ตรวจสอบสลิปผ่าน EasySlip API | `WalletController`, `PromptPayService` |
| **การโอนเงิน (Transfer)** | โอนเงินระหว่างผู้ใช้ในระบบ, ตรวจสอบยอดเงินก่อนโอน | `WalletTransfer` model |
| **ประวัติธุรกรรม (Transaction History)** | บันทึกทุกธุรกรรม (เติมเงิน, จ่ายเงิน, โอนเงิน, รับเงิน), แสดงประวัติ | `Transaction` model |
| **แอดมินจัดการธุรกรรม (Admin Transactions)** | ดูธุรกรรมทั้งหมดในระบบ, ตรวจสอบความผิดปกติ | `admin/transactions.html` |

**รายละเอียดงานเพิ่มเติม:**
- พัฒนา `WalletController` สำหรับจัดการ Wallet flow ทั้งหมด
- สร้าง `WalletService` พร้อม Transaction Management (Atomic Operations)
- ออกแบบ Data Model: `Wallet`, `Transaction`, `WalletTransfer`
- สร้างหน้ากระเป๋าเงินของผู้ใช้ (`user/wallet.html`) และหน้าธุรกรรมของแอดมิน (`admin/transactions.html`)

---

### 5. ⚙️ ระบบหลังบ้านและการแจ้งเตือน (Admin & Notification)

> ระบบแอดมินสำหรับจัดการแพลตฟอร์ม และระบบแจ้งเตือนแบบ Real-Time ผ่าน WebSocket

**ผู้รับผิดชอบ:**
- **นายเกรียงไกร ประเสริฐ** (663380616-4)

**ขอบเขตงานที่รับผิดชอบ:**

| โมดูล | รายละเอียด | ไฟล์หลักที่เกี่ยวข้อง |
|-------|------------|----------------------|
| **แดชบอร์ดแอดมิน (Admin Dashboard)** | ภาพรวมระบบ, สถิติ, จำนวนสินค้า/ผู้ใช้/คำสั่งซื้อ | `AdminController`, `admin/index.html` |
| **จัดการผู้ใช้ (User Management)** | ดูรายการผู้ใช้ทั้งหมด, แก้ไข/ล็อก/ปลดล็อกบัญชี, เพิ่มแอดมิน | `admin/users.html`, `admin/add_admin.html` |
| **จัดการสินค้า (Product Management Admin)** | เพิ่ม/แก้ไข/ลบสินค้า, จัดหมวดหมู่ | `admin/products.html`, `admin/add_product.html`, `admin/edit_product.html` |
| **จัดการคำสั่งซื้อ (Order Management Admin)** | ดูคำสั่งซื้อทั้งหมด, อัปเดตสถานะ, จัดการ Order | `admin/orders.html` |
| **ระบบแจ้งเตือน Real-Time (Notification)** | แจ้งเตือนแบบ Real-Time ผ่าน WebSocket (STOMP), แจ้งสถานะคำสั่งซื้อ, แจ้งธุรกรรม | `NotificationService`, `WebSocketService`, `Notification` model |
| **ระบบบันทึกกิจกรรม (Admin Logs)** | บันทึกทุกการกระทำของแอดมิน (เพิ่ม/แก้ไข/ลบสินค้า, จัดการผู้ใช้) | `AdminLogService`, `AdminLog` model, `admin/logs.html` |
| **ระบบแชทบอท (Chatbot)** | ระบบ Chatbot สำหรับตอบคำถามผู้ใช้อัตโนมัติ | `ChatbotController` |
| **ตั้งค่าเว็บไซต์ (Site Settings)** | ตั้งค่าทั่วไปของเว็บไซต์ (ชื่อเว็บ, โลโก้, ข้อมูลติดต่อ) | `SiteSettingService`, `SiteSetting` model |
| **ระบบอีเมล (Email Service)** | ส่งอีเมลแจ้งเตือน, ยืนยันตัวตน, รีเซ็ตรหัสผ่าน | `EmailService` |
| **ประเภทการแจ้งเตือน (Notification Types)** | จัดหมวดหมู่ Notification ตามประเภท (สั่งซื้อ, ระบบ, ชุมชน) | `NotificationType` enum |

**รายละเอียดงานเพิ่มเติม:**
- กำหนดค่า WebSocket (STOMP Protocol) สำหรับ Real-Time Notification
- พัฒนา `AdminController` สำหรับ Dashboard และฟังก์ชันจัดการทั้งหมด
- สร้าง `NotificationController` สำหรับจัดการ Notification ฝั่งผู้ใช้
- ออกแบบหน้า Notification (`user/notifications.html`) พร้อมแสดงผลแบบ Real-Time
- พัฒนาหน้าแอดมินทั้งหมด: Dashboard, Products, Users, Orders, Logs, Transactions, Profile
- เขียน `AdminLogService` สำหรับบันทึก Audit Trail ทุกการกระทำของแอดมิน
- พัฒนา `ChatbotController` สำหรับการตอบกลับอัตโนมัติ

---

## 📊 สรุปหน้าที่ตามรายบุคคล

### 1. นายเกรียงไกร ประเสริฐ (663380616-4) — Team Lead

| ระบบ | หน้าที่หลัก |
|------|------------|
| 🛒 E-Commerce | Backend API, Business Logic, Payment (PromptPay), Secure Digital Delivery |
| 💰 Digital Wallet | ระบบกระเป๋าเงินทั้งหมด (เติมเงิน, โอนเงิน, ประวัติธุรกรรม) |
| ⚙️ Admin & Notification | Dashboard, จัดการผู้ใช้/สินค้า/คำสั่งซื้อ, Real-Time Notification, Chatbot, Logs, Email |

**ไฟล์ที่พัฒนาหลัก:** `AdminController`, `WalletController`, `NotificationController`, `ChatbotController`, `WalletService`, `WebSocketService`, `SecureDeliveryService`, `AdminLogService`, `EmailService`

---

### 2. นายกิตติกร เสวกวิหารี (663380587-5)

| ระบบ | หน้าที่หลัก |
|------|------------|
| 💬 Community | ระบบชุมชนทั้งหมด (โพสต์, ความคิดเห็น, ถูกใจ, แอดมินจัดการ) |

**ไฟล์ที่พัฒนาหลัก:** `CommunityController`, `AdminCommunityController`, `CommunityService`, `CommunityPostService`, `Post`, `Comment`, `Like` models

---

### 3. นางสาวตติยา บุตรเฟื้อย (663380594-8)

| ระบบ | หน้าที่หลัก |
|------|------------|
| 🔒 Security & 2FA | ระบบความปลอดภัยทั้งหมด (Authentication, OTP, Account Locking, Login Logs, RBAC) |

**ไฟล์ที่พัฒนาหลัก:** `OtpController`, `OtpService`, `LoginLogService`, `UserDetailsServiceImpl`, Security Configuration, Unit Tests (JUnit 5)

---

### 4. นายปาณวัฒน์ จันทร์ทองหลาง (663380390-4)

| ระบบ | หน้าที่หลัก |
|------|------------|
| 🛒 E-Commerce | Frontend UI/UX (Thymeleaf Templates) — หน้าแสดงสินค้า, ตะกร้า, สั่งซื้อ |

**ไฟล์ที่พัฒนาหลัก:** `user/cart.html`, `user/order.html`, `user/home.html`, `guest/view_product.html`, `guest/product.html`, `guest/index.html`

---

### 5. นายปิติ มูลเทพพิชัย (663380045-1)

| ระบบ | หน้าที่หลัก |
|------|------------|
| 🛒 E-Commerce | ระบบสั่งซื้อ, ตรวจสอบสลิป (EasySlip), คลังเกม (Game Library) |

**ไฟล์ที่พัฒนาหลัก:** `GameLibraryController`, `EasySlipService`, `GameLibraryService`, `OrderService`, `user/game_library.html`

---

## 🏗️ สถาปัตยกรรมของระบบ (System Architecture)

```
┌─────────────────────────────────────────────────────┐
│                   Frontend (Thymeleaf)              │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐   │
│  │  Guest   │  │   User   │  │      Admin       │   │
│  │  Pages   │  │  Pages   │  │      Pages       │   │
│  └──────────┘  └──────────┘  └──────────────────┘   │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│              Spring Boot Application                │
│  ┌────────────────────────────────────────────────┐ │
│  │              Controllers Layer                 │ │
│  │  Home │ User │ Admin │ Wallet │ Community │ OTP│ │
│  │  Notification │ GameLibrary │ Chatbot          │ │
│  └────────────────────┬───────────────────────────┘ │
│  ┌────────────────────▼───────────────────────────┐ │
│  │              Service Layer                     │ │
│  │  Product │ Cart │ Order │ Wallet │ Community   │ │
│  │  OTP │ Email │ Notification │ AdminLog         │ │
│  │  PromptPay │ EasySlip │ SecureDelivery         │ │
│  │  GameLibrary │ LoginLog │ WebSocket            │ │
│  └────────────────────┬───────────────────────────┘ │
│  ┌────────────────────▼───────────────────────────┐ │
│  │            Repository Layer (JPA)              │ │
│  └────────────────────┬───────────────────────────┘ │
│  ┌────────────────────▼───────────────────────────┐ │
│  │           Security (Spring Security)           │ │
│  │         WebSocket (STOMP Protocol)             │ │
│  └────────────────────────────────────────────────┘ │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│              External Services                      │
│  ┌────────┐  ┌──────────┐  ┌──────────────────────┐ │
│  │ MySQL  │  │  AWS S3  │  │  EasySlip API        │ │
│  │   DB   │  │ (Images) │  │  (Slip Verification) │ │
│  └────────┘  └──────────┘  └──────────────────────┘ │
└─────────────────────────────────────────────────────┘




```

---

## 📈 สรุปจำนวนงาน

| ประเภท | จำนวน |
|--------|:-----:|
| Controllers | 10 |
| Services | 20+ |
| Models (Entities) | 18 |
| Thymeleaf Templates | 50+ |
| Unit Tests | มี (JUnit 5 + H2 Database) |
| External API Integrations | 2 (EasySlip, AWS S3) |

---

> 📅 **อัปเดตล่าสุด:** 11 มีนาคม 2026
