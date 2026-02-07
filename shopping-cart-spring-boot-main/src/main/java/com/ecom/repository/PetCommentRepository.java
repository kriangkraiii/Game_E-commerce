package com.ecom.repository;

import com.ecom.model.Pet;
import com.ecom.model.PetComment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PetCommentRepository extends JpaRepository<PetComment, Long> {

    List<PetComment> findByPetOrderByCreatedAtDesc(Pet pet);
    long countByPet(Pet pet);

    @Query("SELECT c FROM PetComment c WHERE c.post.id = :postId ORDER BY c.createdAt DESC")
    List<PetComment> findByPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(c) FROM PetComment c WHERE c.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);

    // ลบ @Transactional ออกจาก Repository
    @Modifying
    @Query("DELETE FROM PetComment c WHERE c.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("DELETE FROM PetComment c WHERE c.pet = :pet")
    void deleteByPet(@Param("pet") Pet pet);

    @Query("SELECT c FROM PetComment c WHERE c.id = :commentId AND c.user.id = :userId")
    Optional<PetComment> findByIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Integer userId);
}
