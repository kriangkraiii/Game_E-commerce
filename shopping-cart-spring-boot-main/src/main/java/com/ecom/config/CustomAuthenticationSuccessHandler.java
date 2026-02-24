package com.ecom.config;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.EmailService;
import com.ecom.service.LoginLogService;
import com.ecom.service.OtpService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private LoginLogService loginLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        String email = authentication.getName();
        UserDtls user = userRepository.findByEmail(email);

        if (user.isOtpEnabled()) {
            // OTP เปิดอยู่ → ส่ง OTP และ redirect ไปหน้ายืนยัน
            String otp = otpService.generateOtp(user);
            emailService.sendOtpEmail(user.getEmail(), otp);
            request.getSession().setAttribute("otpUser", user.getEmail());
            response.sendRedirect("/verify-otp");
        } else {
            // OTP ปิดอยู่ → ล็อกอินตรงเลย บันทึก login log
            loginLogService.saveLoginLog(user);
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            response.sendRedirect(isAdmin ? "/admin/" : "/");
        }
    }
}
