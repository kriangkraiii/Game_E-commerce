package com.ecom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.OtpService;

import jakarta.servlet.http.HttpSession;

@Controller
public class OtpController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @GetMapping("/verify-otp")
    public String showOtpPage() {
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

            if (user.getRole().equals("ROLE_ADMIN"))
                return "redirect:/admin/";
            else
                return "redirect:/";

        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid or Expired OTP");
            return "redirect:/verify-otp";
        }
    }
}

