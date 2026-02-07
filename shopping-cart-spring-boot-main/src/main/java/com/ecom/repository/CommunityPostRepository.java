package com.ecom.repository;

import com.ecom.model.CommunityPost;
import com.ecom.model.Pet;
import com.ecom.model.UserDtls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    List<CommunityPost> findAllByOrderByCreatedAtDesc();
    List<CommunityPost> findByUserOrderByCreatedAtDesc(Pet pet);
   
 // เพิ่มเมธอดนี้
    List<CommunityPost> findByPetOrderByCreatedAtDesc(Pet pet);
}
