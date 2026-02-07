package com.ecom.repository;

import com.ecom.model.CommunityPost;
import com.ecom.model.Notification;
import com.ecom.model.UserDtls;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(UserDtls recipient);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false")
    long countUnreadByRecipient(@Param("recipient") UserDtls recipient);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false")
    void markAllAsReadByRecipient(@Param("recipient") UserDtls recipient);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);

    // Hard delete by postId
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    // Hard delete by petId
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.pet.id = :petId")
    void deleteByPetId(@Param("petId") Integer petId);
    
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.post = :post")
    void deleteByPost(@Param("post") CommunityPost post);

}
