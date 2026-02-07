package com.ecom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "post_likes")
public class PostLike {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "post_id", nullable = false)
	private CommunityPost post;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private UserDtls user;

	// Constructors
	public PostLike() {
	}

	public PostLike(CommunityPost post, UserDtls user) {
		this.post = post;
		this.user = user;
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
}
