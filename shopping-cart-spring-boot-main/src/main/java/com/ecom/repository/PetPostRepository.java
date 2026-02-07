package com.ecom.repository;

import com.ecom.model.PetPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PetPostRepository extends JpaRepository<PetPost, Integer> {
	List<PetPost> findByPetId(Integer petId);
}
