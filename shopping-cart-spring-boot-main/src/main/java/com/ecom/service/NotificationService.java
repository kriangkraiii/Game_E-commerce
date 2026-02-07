package com.ecom.service;

import com.ecom.model.*;
import java.util.List;

public interface NotificationService {
	void createLikeNotification(UserDtls actor, Pet pet, CommunityPost post);

	void createCommentNotification(UserDtls actor, Pet pet, CommunityPost post, String commentContent);

	List<Notification> getUserNotifications(UserDtls user);

	long getUnreadCount(UserDtls user);

	void markAsRead(Long notificationId);

	void markAllAsRead(UserDtls user);
}