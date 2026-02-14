package com.ecom.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.model.Like;
import com.ecom.model.Post;
import com.ecom.model.UserDtls;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostAndUser(Post post, UserDtls user);

    Long countByPost(Post post);

    boolean existsByPostAndUser(Post post, UserDtls user);
}
