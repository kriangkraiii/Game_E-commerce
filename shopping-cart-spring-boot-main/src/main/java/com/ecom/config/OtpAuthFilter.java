package com.ecom.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Filter ที่ตรวจสอบว่าผู้ใช้ได้ยืนยัน OTP แล้วหรือยัง
 * ถ้ายังไม่ยืนยัน (otpUser session ยังอยู่) จะ redirect ไป /verify-otp
 * ป้องกันการกดปุ่มย้อนกลับเพื่อข้าม OTP
 */
@Component
public class OtpAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // อนุญาตให้ผ่านสำหรับ endpoints เหล่านี้
        if (isWhitelisted(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);

        // ถ้า session มี otpUser = ยังไม่ยืนยัน OTP → redirect ไป verify-otp
        if (session != null && session.getAttribute("otpUser") != null) {
            response.sendRedirect(request.getContextPath() + "/verify-otp");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String uri) {
        return uri.equals("/verify-otp")
                || uri.equals("/logout")
                || uri.startsWith("/login")          // Spring Security login processing
                || uri.startsWith("/css")
                || uri.startsWith("/js")
                || uri.startsWith("/img")
                || uri.startsWith("/static")
                || uri.startsWith("/admin/css")
                || uri.startsWith("/admin/js")
                || uri.startsWith("/admin/img")
                || uri.startsWith("/uploads")
                || uri.startsWith("/favicon");
    }
}
