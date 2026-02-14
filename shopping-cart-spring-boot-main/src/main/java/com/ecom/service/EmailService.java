package com.ecom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("🔐 Your OTP Code");

            String htmlContent = """
                    <div style="font-family: Arial; background-color: #f4f4f4; padding: 30px;">
                        <div style="max-width: 500px; margin: auto; background: white; padding: 30px; border-radius: 10px;">
                            <h2 style="color:#ff6600;">Game Store Verification</h2>
                            <p>Hello,</p>
                            <p>Your OTP code is:</p>

                            <div style="
                                font-size: 28px;
                                font-weight: bold;
                                letter-spacing: 5px;
                                color: #ffffff;
                                background-color: #ff6600;
                                padding: 15px;
                                text-align: center;
                                border-radius: 8px;
                            ">
                                """
                    + otp + """
                                    </div>

                                    <p style="margin-top:20px;">⏳ This code will expire in 5 minutes.</p>

                                    <hr>
                                    <small style="color:gray;">
                                        If you did not request this, please ignore this email.
                                    </small>
                                </div>
                            </div>
                            """;

            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
