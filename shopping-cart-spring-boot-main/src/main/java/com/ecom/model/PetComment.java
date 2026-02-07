package com.ecom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PetComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;
    
    @ManyToOne
    @JoinColumn(name = "post_id")
    private CommunityPost post;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDtls user;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_edited")
    private boolean edited = false;
    
    
    @OneToOne
    @JoinColumn(name = "original_comment_id")
    private PetComment originalComment;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }
    
    public CommunityPost getPost() { return post; }
    public void setPost(CommunityPost post) { this.post = post; }
    
    public UserDtls getUser() { return user; }
    public void setUser(UserDtls user) { this.user = user; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }
    
    public PetComment getOriginalComment() { return originalComment; }
    public void setOriginalComment(PetComment originalComment) { this.originalComment = originalComment; }
}
