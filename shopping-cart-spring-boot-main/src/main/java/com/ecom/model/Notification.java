package com.ecom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "recipient_id", nullable = false)
	private UserDtls recipient; // ผู้รับการแจ้งเตือน

	@ManyToOne
	@JoinColumn(name = "actor_id", nullable = false)
	private UserDtls actor; // ผู้ที่ทำการกระทำ

	@ManyToOne
	@JoinColumn(name = "post_id")
	private CommunityPost post; // โพสต์ที่เกี่ยวข้อง

	@ManyToOne
	@JoinColumn(name = "pet_id")
	private Pet pet; // สัตว์เลี้ยงที่เกี่ยวข้อง

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationType type;

	@Column(nullable = false, length = 500)
	private String message;

	@Column(nullable = false)
	private Boolean isRead = false;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	// Constructors
	public Notification() {
	}

	public Notification(UserDtls recipient, UserDtls actor, CommunityPost post, Pet pet, NotificationType type,
			String message) {
		this.recipient = recipient;
		this.actor = actor;
		this.post = post;
		this.pet = pet;
		this.type = type;
		this.message = message;
		this.isRead = false;
		this.createdAt = LocalDateTime.now();
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserDtls getRecipient() {
		return recipient;
	}

	public void setRecipient(UserDtls recipient) {
		this.recipient = recipient;
	}

	public UserDtls getActor() {
		return actor;
	}

	public void setActor(UserDtls actor) {
		this.actor = actor;
	}

	public CommunityPost getPost() {
		return post;
	}

	public void setPost(CommunityPost post) {
		this.post = post;
	}

	public Pet getPet() {
		return pet;
	}

	public void setPet(Pet pet) {
		this.pet = pet;
	}

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getRead() {
		return isRead;
	}

	public void setRead(Boolean read) {
		isRead = read;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}