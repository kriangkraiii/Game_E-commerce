package com.ecom.service.impl;

import com.ecom.model.*;
import com.ecom.repository.NotificationRepository;
import com.ecom.service.NotificationService;
import com.ecom.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private CommonUtil commonUtil;

	@Override
	@Transactional
	public void createLikeNotification(UserDtls actor, Pet pet, CommunityPost post) {
		// ไม่สร้างการแจ้งเตือนถ้าเป็นการกด like โพสต์ของตัวเอง
		if (actor.getId().equals(pet.getOwner().getId())) {
			return;
		}

		String message = actor.getName() + " liked your pet " + pet.getName() + "'s post";

		Notification notification = new Notification(pet.getOwner(), // recipient
				actor, // actor
				post, // post
				pet, // pet
				NotificationType.LIKE, message);

		notificationRepository.save(notification);

		// ส่ง email แจ้งเตือน
		try {
			sendLikeNotificationEmail(pet.getOwner(), actor, pet, post);
		} catch (Exception e) {
			e.printStackTrace();
			// Log error but don't fail the notification creation
		}
	}

	@Override
	@Transactional
	public void createCommentNotification(UserDtls actor, Pet pet, CommunityPost post, String commentContent) {
		// ไม่สร้างการแจ้งเตือนถ้าเป็นการ comment โพสต์ของตัวเอง
		if (actor.getId().equals(pet.getOwner().getId())) {
			return;
		}

		String message = actor.getName() + " commented on your pet " + pet.getName() + "'s post";

		Notification notification = new Notification(pet.getOwner(), // recipient
				actor, // actor
				post, // post
				pet, // pet
				NotificationType.COMMENT, message);

		notificationRepository.save(notification);

		// ส่ง email แจ้งเตือน
		try {
			sendCommentNotificationEmail(pet.getOwner(), actor, pet, post, commentContent);
		} catch (Exception e) {
			e.printStackTrace();
			// Log error but don't fail the notification creation
		}
	}

	@Override
	public List<Notification> getUserNotifications(UserDtls user) {
		return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
	}

	@Override
	public long getUnreadCount(UserDtls user) {
		return notificationRepository.countUnreadByRecipient(user);
	}

	@Override
	@Transactional
	public void markAsRead(Long notificationId) {
		notificationRepository.markAsRead(notificationId);
	}

	@Override
	@Transactional
	public void markAllAsRead(UserDtls user) {
		notificationRepository.markAllAsReadByRecipient(user);
	}

	private void sendLikeNotificationEmail(UserDtls owner, UserDtls actor, Pet pet, CommunityPost post)
			throws UnsupportedEncodingException, MessagingException {

		String subject = "Someone liked your pet's post!";
		String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
				+ "<h2 style='color: #3b82f6;'>🐾 Pet Community Notification</h2>" + "<p>Hello " + owner.getName()
				+ ",</p>" + "<p><strong>" + actor.getName() + "</strong> liked your pet <strong>" + pet.getName()
				+ "</strong>'s post!</p>"
				+ "<div style='background: #f8fafc; padding: 20px; border-radius: 8px; margin: 20px 0;'>"
				+ "<h4>Post Details:</h4>" + "<p><strong>Pet:</strong> " + pet.getName() + " (" + pet.getType()
				+ ")</p>" + "<p><strong>Description:</strong> "
				+ (post.getDescription() != null ? post.getDescription() : "No description") + "</p>" + "</div>"
				+ "<p>Click <a href='http://localhost:8080/community/post/" + post.getId()
				+ "' style='color: #3b82f6; text-decoration: none;'>here</a> to view your post.</p>"
				+ "<p>Best regards,Pet Community Team</p>" + "</div>";

		commonUtil.sendNotificationEmail(owner.getEmail(), subject, content);
	}

	private void sendCommentNotificationEmail(UserDtls owner, UserDtls actor, Pet pet, CommunityPost post,
			String commentContent) throws UnsupportedEncodingException, MessagingException {

		String subject = "New comment on your pet's post!";
		String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
				+ "<h2 style='color: #3b82f6;'>🐾 Pet Community Notification</h2>" + "<p>Hello " + owner.getName()
				+ ",</p>" + "<p><strong>" + actor.getName() + "</strong> commented on your pet <strong>" + pet.getName()
				+ "</strong>'s post!</p>"
				+ "<div style='background: #f8fafc; padding: 20px; border-radius: 8px; margin: 20px 0;'>"
				+ "<h4>Comment:</h4>"
				+ "<p style='font-style: italic; background: white; padding: 15px; border-left: 4px solid #3b82f6;'>"
				+ "\"" + commentContent + "\"" + "</p>" + "</div>"
				+ "<div style='background: #f1f5f9; padding: 15px; border-radius: 8px; margin: 20px 0;'>"
				+ "<h4>Post Details:</h4>" + "<p><strong>Pet:</strong> " + pet.getName() + " (" + pet.getType()
				+ ")</p>" + "<p><strong>Description:</strong> "
				+ (post.getDescription() != null ? post.getDescription() : "No description") + "</p>" + "</div>"
				+ "<p>Click <a href='http://localhost:8080/community/post/" + post.getId()
				+ "/comments' style='color: #3b82f6; text-decoration: none;'>here</a> to view the comment and reply.</p>"
				+ "<p>Best regards,Pet Community Team</p>" + "</div>";

		commonUtil.sendNotificationEmail(owner.getEmail(), subject, content);
	}
}