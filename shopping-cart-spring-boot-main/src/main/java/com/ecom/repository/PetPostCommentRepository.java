package com.ecom.repository;

import com.ecom.model.PetPostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetPostCommentRepository extends JpaRepository<PetPostComment, Integer> {

    List<PetPostComment> findByPostIdOrderByCreatedAtAsc(Integer postId);

    long countByPostId(Integer postId);

    void deleteByPostId(Integer postId);
}
