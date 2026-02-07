package com.ecom.model;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "post_id", "user_id" }))
public class PetPostLike {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "post_id")
	private PetPost post;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserDtls user;

	public PetPostLike() {
	}

	public PetPostLike(PetPost post, UserDtls user) {
		this.post = post;
		this.user = user;
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

	// getters/setters
}
