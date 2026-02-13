package com.ecom.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;

@Service
public class OtpService {

    @Autowired
    private UserRepository userRepository;

    public String generateOtp(UserDtls user) {

        String otp = String.valueOf((int)((Math.random() * 900000) + 100000));

        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        user.setOtpVerified(false);

        userRepository.save(user);

        return otp;
    }

    public boolean validateOtp(UserDtls user, String inputOtp) {

        if (user.getOtp() == null) return false;

        if (user.getOtpExpiryTime().isBefore(LocalDateTime.now()))
            return false;

        if (!user.getOtp().equals(inputOtp))
            return false;

        user.setOtpVerified(true);
        user.setOtp(null);
        userRepository.save(user);

        return true;
    }
}
