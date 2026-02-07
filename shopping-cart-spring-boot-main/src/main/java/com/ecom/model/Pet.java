package com.ecom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Pet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotBlank
    @Size(min = 1, max = 50)
	private String name;
	@NotBlank
	@Size(min = 1, max = 30)
	private String type;
	@NotBlank
	private String breed;
//	private Integer age;
	private String imagePet;
	private String description;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserDtls owner;
	
	@Transient
	private long likeCount;

	@Transient
	private boolean likedByCurrentUser;
	
	

	public Pet(Integer id, @NotBlank @Size(min = 1, max = 50) String name,
			@NotBlank @Size(min = 1, max = 30) String type, @NotBlank String breed, String imagePet, String description,
			UserDtls owner, long likeCount, boolean likedByCurrentUser) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.breed = breed;
		this.imagePet = imagePet;
		this.description = description;
		this.owner = owner;
		this.likeCount = likeCount;
		this.likedByCurrentUser = likedByCurrentUser;
	}

	public long getLikeCount() {
	    return likeCount;
	}

	public void setLikeCount(long likeCount) {
	    this.likeCount = likeCount;
	}

	public boolean isLikedByCurrentUser() {
	    return likedByCurrentUser;
	}

	public void setLikedByCurrentUser(boolean likedByCurrentUser) {
	    this.likedByCurrentUser = likedByCurrentUser;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBreed() {
		return breed;
	}

	public void setBreed(String breed) {
		this.breed = breed;
	}

//	public Integer getAge() {
//		return age;
//	}
//
//	public void setAge(Integer age) {
//		this.age = age;
//	}

	public String getImagePet() {
		return imagePet;
	}

	public void setImagePet(String imagePet) {
		this.imagePet = imagePet;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}



	public UserDtls getOwner() {
		return owner;
	}

	public void setOwner(UserDtls owner) {
		this.owner = owner;
	}

	

	@Override
	public String toString() {
		return "Pet [id=" + id + ", name=" + name + ", type=" + type + ", breed=" + breed + ", imagePet=" + imagePet
				+ ", description=" + description + ", owner=" + owner + ", likeCount=" + likeCount
				+ ", likedByCurrentUser=" + likedByCurrentUser + "]";
	}

	
	
	public Pet() {
		// TODO Auto-generated constructor stub
	}

	

}
