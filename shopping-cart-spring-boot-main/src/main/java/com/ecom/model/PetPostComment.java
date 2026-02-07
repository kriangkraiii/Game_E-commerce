package com.ecom.model;

import jakarta.persistence.*; 
import java.time.LocalDateTime;

@Entity
public class PetPostComment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "post_id")
	private PetPost post;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserDtls user;

	@Column(length = 2000)
	private String content;

	private LocalDateTime createdAt = LocalDateTime.now();

	public PetPostComment() {}

	public PetPostComment(PetPost post, UserDtls user, String content) {
	    this.post = post;
	    this.user = user;
	    this.content = content;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public PetPost getPost() {
		return post;
	}

	public void setPost(PetPost post) {
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

	// getters/setters
}
