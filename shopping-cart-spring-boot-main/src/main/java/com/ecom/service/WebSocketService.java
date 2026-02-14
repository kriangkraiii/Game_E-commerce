package com.ecom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.ecom.model.Notification;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String username, Notification notification) {
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notification);
    }
}
