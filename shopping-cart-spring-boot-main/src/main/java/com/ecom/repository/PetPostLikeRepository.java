package com.ecom.repository;

import com.ecom.model.PetPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetPostLikeRepository extends JpaRepository<PetPostLike, Integer> {

    long countByPostId(Integer postId);

    void deleteByPostId(Integer postId);
}

