package com.ecom.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecom.model.Comment;
import com.ecom.model.Like;
import com.ecom.model.Notification;
import com.ecom.model.NotificationType;
import com.ecom.model.Post;
import com.ecom.model.UserDtls;
import com.ecom.repository.CommentRepository;
import com.ecom.repository.LikeRepository;
import com.ecom.repository.PostRepository;
import com.ecom.util.CommonUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class CommunityService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private WebSocketService webSocketService;

    // We need a way to save notifications. Assuming NotificationRepository exists
    // from previous features
    // If not, we might need to rely on what's available or create it.
    // Based on file list, Notification.java exists, so likely
    // NotificationRepository or Service exists?
    // Let's assume we can wire it here.
    // Checking file list... repository folder has 12 files.
    // If NotificationRepository is missing, I should create it.
    // I will assume it might be missing and add it to the plan if needed, but for
    // now I will inject it.
    // Wait, I didn't see NotificationRepository in the file list earlier.
    // Let me check if NotificationService exists.

    // START_TEMPORARY_FIX: I will assume I need to create NotificationRepository if
    // it's not there.
    // But for this file I will autowire it.
    @Autowired
    private com.ecom.repository.NotificationRepository notificationRepository;

    @Autowired
    private CommonUtil commonUtil;

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public Page<Post> getAllPostsPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Post createPost(String content, UserDtls user) {
        Post post = new Post(content, user);
        return postRepository.save(post);
    }

    @Transactional
    public boolean toggleLike(Long postId, UserDtls user) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        Optional<Like> existingLike = likeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return false; // unmatched (unliked)
        } else {
            Like like = new Like(post, user);
            likeRepository.save(like);

            // Send Notification if liker is not the author
            if (!post.getAuthor().getId().equals(user.getId())) {
                sendNotification(post.getAuthor(), user, NotificationType.POST_LIKE,
                        user.getName() + " liked your post", post);
            }
            return true; // liked
        }
    }

    public Comment addComment(Long postId, String content, UserDtls user) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        Comment comment = new Comment(content, post, user);
        Comment savedComment = commentRepository.save(comment);

        // Send Notification if commenter is not the author
        if (!post.getAuthor().getId().equals(user.getId())) {
            sendNotification(post.getAuthor(), user, NotificationType.POST_COMMENT,
                    user.getName() + " commented on your post: " + content, post);
        }

        return savedComment;
    }

    private void sendNotification(UserDtls recipient, UserDtls actor, NotificationType type, String message,
            Post post) {
        // Save to database with post reference
        Notification notification = new Notification(recipient, actor, type, message, post);
        notificationRepository.save(notification);

        // Send Real-time via WebSocket
        try {
            webSocketService.sendNotification(recipient.getEmail(), notification);
        } catch (Exception e) {
            System.err.println("WebSocket notification failed: " + e.getMessage());
        }

        // Send Email Notification (Professional Template like forgot-password)
        try {
            String subject = "📢 Community Notification - Game Store";

            String emailContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                    + "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px; text-align: center;'>"
                    + "<h1 style='color: white; margin: 0;'>🎮 Game Store Community</h1>"
                    + "</div>"
                    + "<div style='padding: 30px; background-color: #f8f9fa;'>"
                    + "<h2 style='color: #333;'>Hello " + recipient.getName() + ",</h2>"
                    + "<p style='font-size: 16px; color: #555;'>You have a new notification from our community!</p>"
                    + "<div style='background-color: white; border-left: 4px solid #667eea; padding: 15px; margin: 20px 0;'>"
                    + "<p style='margin: 0; font-size: 18px;'><strong>" + actor.getName() + "</strong> " + message
                    + "</p>"
                    + "</div>"
                    + "<p style='font-size: 14px; color: #777;'>Visit our community to see the full activity and interact with other gamers!</p>"
                    + "<div style='text-align: center; margin-top: 30px;'>"
                    + "<a href='http://localhost:8080/user/community' style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;'>View Community</a>"
                    + "</div>"
                    + "</div>"
                    + "<div style='padding: 15px; text-align: center; background-color: #333; color: #aaa; font-size: 12px;'>"
                    + "<p>© 2026 Game Store. All rights reserved.</p>"
                    + "<p>This email was sent to " + recipient.getEmail() + "</p>"
                    + "</div>"
                    + "</div>";

            Boolean emailSent = commonUtil.sendNotificationEmail(recipient.getEmail(), subject, emailContent);
            if (emailSent) {
                System.out.println("Email notification sent to: " + recipient.getEmail());
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to send email notification to " + recipient.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Long getLikeCount(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        return likeRepository.countByPost(post);
    }

    public List<Post> getPostsByUser(UserDtls user) {
        return postRepository.findByAuthorOrderByCreatedAtDesc(user);
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Transactional
    public Post updatePost(Long postId, String content, UserDtls user) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getAuthor().getId().equals(user.getId()) && !user.getRole().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Unauthorized: You can only edit your own posts");
        }
        post.setContent(content);
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId, UserDtls user) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getAuthor().getId().equals(user.getId()) && !user.getRole().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Unauthorized: You can only delete your own posts");
        }
        postRepository.delete(post);
    }

    // Twitter-style reply to comment
    public Comment addReply(Long parentCommentId, String content, UserDtls user) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        Comment reply = new Comment(content, parentComment.getPost(), user);
        reply.setParentComment(parentComment);
        Comment savedReply = commentRepository.save(reply);

        // Notify the parent comment author
        if (!parentComment.getAuthor().getId().equals(user.getId())) {
            sendNotification(parentComment.getAuthor(), user, NotificationType.POST_COMMENT,
                    user.getName() + " replied to your comment: " + content, parentComment.getPost());
        }

        return savedReply;
    }

    // Edit comment (only by comment author or admin)
    @Transactional
    public Comment updateComment(Long commentId, String content, UserDtls user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        if (!comment.getAuthor().getId().equals(user.getId()) && !user.getRole().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Unauthorized: You can only edit your own comments");
        }
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    // Delete comment (only by comment author or admin)
    @Transactional
    public void deleteComment(Long commentId, UserDtls user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        if (!comment.getAuthor().getId().equals(user.getId()) && !user.getRole().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Unauthorized: You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }
}
