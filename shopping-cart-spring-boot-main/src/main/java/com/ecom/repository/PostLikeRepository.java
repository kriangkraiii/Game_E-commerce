package com.ecom.repository;

import com.ecom.model.CommunityPost;
import com.ecom.model.PostLike;
import com.ecom.model.UserDtls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByUserAndPost(UserDtls user, CommunityPost post);

    PostLike findByUserAndPost(UserDtls user, CommunityPost post);

    long countByPost(CommunityPost post);

    

    void deleteByPostId(Long postId);
    
    @Modifying
    @Query("DELETE FROM PostLike p WHERE p.post = :post")
    void deleteByPost(@Param("post") CommunityPost post);

}
