package com.ecom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PetPost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String title;
	private String content;
	private String ImagePath;

	@ManyToOne
	@JoinColumn(name = "pet_id")
	private Pet pet;

	// Constructors
	public PetPost() {
	}

	public PetPost(Integer id, String title, String content, String imageUrl, Pet pet) {
		this.id = id;
		this.title = title;
		this.content = content;
		this.ImagePath = imageUrl;
		this.pet = pet;
	}

	// Getters & Setters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getImageUrl() {
		return ImagePath;
	}

	public void setImageUrl(String imageUrl) {
		this.ImagePath = imageUrl;
	}

	public Pet getPet() {
		return pet;
	}

	public void setPet(Pet pet) {
		this.pet = pet;
	}
}

