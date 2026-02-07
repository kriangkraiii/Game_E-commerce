package com.ecom.repository;

import com.ecom.model.CommunityPost;
import com.ecom.model.PetComment;
import com.ecom.model.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
	List<PostComment> findByPostOrderByCreatedAtDesc(CommunityPost post);

	long countByPost(CommunityPost post);
	@Query("SELECT c FROM PetComment c WHERE c.id = :commentId AND c.user.id = :userId")
	Optional<PetComment> findByIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Integer userId);

}
