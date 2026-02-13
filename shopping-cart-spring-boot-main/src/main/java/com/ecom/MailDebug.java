package com.ecom;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailDebug {

    @Value("${spring.mail.username}")
    private String mailUser;

    @PostConstruct
    public void testMail() {
        System.out.println("MAIL USER = " + mailUser);
    }
}
