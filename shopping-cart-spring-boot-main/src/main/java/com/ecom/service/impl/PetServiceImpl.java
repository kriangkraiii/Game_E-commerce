package com.ecom.service.impl;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.CommunityPost;
import com.ecom.model.Pet;
import com.ecom.model.PetPost;
import com.ecom.model.UserDtls;
import com.ecom.repository.CommunityPostRepository;
import com.ecom.repository.NotificationRepository;
import com.ecom.repository.PetCommentRepository;
import com.ecom.repository.PetLikeRepository;
import com.ecom.repository.PetPostCommentRepository;
import com.ecom.repository.PetPostLikeRepository;
import com.ecom.repository.PetPostRepository;
import com.ecom.repository.PetRepository;
import com.ecom.repository.PostLikeRepository;
import com.ecom.service.PetService;

import jakarta.transaction.Transactional;

@Service
public class PetServiceImpl implements PetService {

	@Autowired
	private PetRepository petRepository;
	
	@Autowired
	private CommunityPostRepository communityPostRepository;

	@Autowired
	private PostLikeRepository postLikeRepository;

	@Autowired
	private PetCommentRepository petCommentRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private PetLikeRepository petLikeRepository;

	@Autowired
	private PetPostRepository petPostRepository;

	@Autowired
	private PetPostLikeRepository petPostLikeRepository;

	@Autowired
	private PetPostCommentRepository petPostCommentRepository;

	@Override
	public List<Pet> getUserPets(UserDtls user) {
		return petRepository.findByOwner(user);
	}

	@Override
	public void addPet(String name, String type, String breed, UserDtls owner, String description, String imagePet) {
	    Pet pet = new Pet();
	    pet.setName(name);
	    pet.setType(type);
	    pet.setBreed(breed);
	 
	    pet.setOwner(owner); // บันทึก owner (user ที่ล็อกอิน)
	    pet.setDescription(description);
	    pet.setImagePet(imagePet);
	    petRepository.save(pet);
	}



	@Override
	public Pet getPetById(int id) {
		return petRepository.findById(id).orElse(null);
	}

	@Override
	public void updatePet(Pet pet) {
		        petRepository.save(pet);
		
	}
	
	@Override
	public List<Pet> getAllPets() {
		return petRepository.findAll();
	}

	@Override
	public void addPet(Pet pet) {
		petRepository.save(pet);
	}

	@Override
	public List<Pet> getPetsByOwner(UserDtls owner) {
		return petRepository.findByOwner(owner);
	}

	@Override
	public void savePet(Pet pet) {
		petRepository.save(pet);
	}

	/**
	 * ลบ Pet แบบปลอดภัย: เคลียร์ความสัมพันธ์ทั้งหมด (posts, likes, comments,
	 * notifications, pet-posts) ก่อนลบ pet จริง เพื่อไม่ให้ชน Foreign Key
	 * constraints
	 */
	@Override
	@Transactional
	public void deletePet(int id) {
		Pet pet = petRepository.findById(id).orElse(null);
		if (pet == null) {
			return;
		}

		// 1) ลบ CommunityPost ทั้งหมดของสัตว์ตัวนี้ พร้อมลูก
		// (comments/likes/notifications/ไฟล์รูป)
		List<CommunityPost> posts = communityPostRepository.findByPetOrderByCreatedAtDesc(pet);
		for (CommunityPost post : posts) {
			Long postId = post.getId();

			// 1.1 ลบ comments ที่ผูกกับโพสต์
			petCommentRepository.deleteByPostId(postId);

			// 1.2 ลบ likes ของโพสต์ (PostLike)
			postLikeRepository.deleteByPostId(postId);

			// 1.3 ลบ notifications ที่ผูกกับโพสต์
			notificationRepository.deleteByPostId(postId);

			// 1.4 ลบไฟล์รูปของโพสต์ ถ้ามี
			String postImage = post.getPostImage();
			if (postImage != null && !postImage.isBlank()) {
				try {
					Files.deleteIfExists(Paths.get("src/main/resources/static" + postImage));
				} catch (Exception ignored) {
				}
			}

			// 1.5 ลบโพสต์
			communityPostRepository.delete(post);
		}

		// 2) ลบ likes ที่อิงระดับ "Pet" โดยตรง (PetLike)
		petLikeRepository.deleteByPet(pet);

		// 3) ลบ comments ที่อิงระดับ "Pet" โดยตรง (ถ้ามีกรณีนี้ในระบบ)
		petCommentRepository.deleteByPet(pet);

		// 4) ลบ notifications ที่ยังอ้างอิง Pet นี้ (กันกรณีเหลือเศษ)
		notificationRepository.deleteByPetId(pet.getId());

		// 5) ลบ PetPost (ถ้าคุณใช้งานส่วนนี้) พร้อม likes/comments/ไฟล์รูป
		List<PetPost> petPosts = petPostRepository.findByPetId(pet.getId());
		for (PetPost pp : petPosts) {
			Integer postId = pp.getId();

			petPostLikeRepository.deleteByPostId(postId);
			petPostCommentRepository.deleteByPostId(postId);

			// ลบรูปภาพของ PetPost ถ้าไม่ใช่รูป default
			String imageUrl = pp.getImageUrl();
			if (imageUrl != null && !imageUrl.isBlank() && imageUrl.startsWith("/img/")
					&& !imageUrl.equals("/img/pet_img/default.jpg")) {
				try {
					Files.deleteIfExists(Paths.get("src/main/resources/static" + imageUrl));
				} catch (Exception ignored) {
				}
			}

			petPostRepository.delete(pp);
		}

		// 6) ลบไฟล์รูปของ Pet ถ้าไม่ใช่ default
		String petImage = pet.getImagePet();
		if (petImage != null && !petImage.equals("/img/pet_img/default.jpg")) {
			try {
				Files.deleteIfExists(Paths.get("src/main/resources/static" + petImage));
			} catch (Exception ignored) {
			}
		}

		// 7) ลบ Pet
		petRepository.delete(pet);
	}

	@Override
	public boolean deletePetWithDependencies(Integer petId) {
		// TODO Auto-generated method stub
		return false;
	}
}