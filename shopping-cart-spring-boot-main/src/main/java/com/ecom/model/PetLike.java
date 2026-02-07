package com.ecom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PetLike {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "pet_id", nullable = false)
	private Pet pet;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private UserDtls user;

	// Constructors, getters, setters
	public PetLike() {
	}

	public PetLike(Pet pet, UserDtls user) {
		this.pet = pet;
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public Pet getPet() {
		return pet;
	}

	public void setPet(Pet pet) {
		this.pet = pet;
	}

	public UserDtls getUser() {
		return user;
	}

	public void setUser(UserDtls user) {
		this.user = user;
	}
}

