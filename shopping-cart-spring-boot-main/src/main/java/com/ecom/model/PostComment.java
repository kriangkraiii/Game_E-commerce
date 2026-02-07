package com.ecom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_comments")
public class PostComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "post_id", nullable = false)
	private CommunityPost post;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private UserDtls user;

	@Column(nullable = false, length = 1000)
	private String content;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	// Constructors
	public PostComment() {
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CommunityPost getPost() {
		return post;
	}

	public void setPost(CommunityPost post) {
		this.post = post;
	}

	public UserDtls getUser() {
		return user;
	}

	public void setUser(UserDtls user) {
		this.user = user;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
