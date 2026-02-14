package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecom.model.Post;
import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.CommunityService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String community(Model model, Principal principal) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        model.addAttribute("user", user);

        List<Post> posts = communityService.getAllPosts();
        model.addAttribute("posts", posts);

        return "user/community";
    }

    // My Posts page - shows only current user's posts
    @GetMapping("/my-posts")
    public String myPosts(Model model, Principal principal) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        model.addAttribute("user", user);

        List<Post> posts = communityService.getPostsByUser(user);
        model.addAttribute("posts", posts);

        return "user/my_posts";
    }

    // Post detail page
    @GetMapping("/post/{postId}")
    public String postDetail(@PathVariable Long postId, Model model, Principal principal) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        model.addAttribute("user", user);

        Post post = communityService.getPostById(postId);
        model.addAttribute("post", post);

        return "user/post_detail";
    }

    @PostMapping("/post")
    public String createPost(@RequestParam String content, Principal principal) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        communityService.createPost(content, user);
        return "redirect:/user/community";
    }

    // Edit post page
    @GetMapping("/post/{postId}/edit")
    public String editPostPage(@PathVariable Long postId, Model model, Principal principal) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        model.addAttribute("user", user);

        Post post = communityService.getPostById(postId);
        if (!post.getAuthor().getId().equals(user.getId())) {
            return "redirect:/user/community";
        }
        model.addAttribute("post", post);
        return "user/edit_post";
    }

    // Update post
    @PostMapping("/post/{postId}/edit")
    public String updatePost(@PathVariable Long postId, @RequestParam String content,
            Principal principal, HttpSession session) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        try {
            communityService.updatePost(postId, content, user);
            session.setAttribute("succMsg", "Post updated successfully!");
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/user/community/post/" + postId;
    }

    // Delete post
    @PostMapping("/post/{postId}/delete")
    public String deletePost(@PathVariable Long postId, Principal principal, HttpSession session) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        try {
            communityService.deletePost(postId, user);
            session.setAttribute("succMsg", "Post deleted successfully!");
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/user/community/my-posts";
    }

    @PostMapping("/like")
    @ResponseBody
    public ResponseEntity<?> toggleLike(@RequestParam Long postId, Principal principal) {
        try {
            String email = principal.getName();
            UserDtls user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            boolean liked = communityService.toggleLike(postId, user);
            Long likeCount = communityService.getLikeCount(postId);
            return ResponseEntity.ok().body(new LikeResponse(liked, likeCount));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/comment")
    public String addComment(@RequestParam Long postId, @RequestParam String content, Principal principal) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        communityService.addComment(postId, content, user);
        return "redirect:/user/community";
    }

    @PostMapping("/reply")
    public String addReply(@RequestParam Long parentCommentId, @RequestParam String content, Principal principal) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        communityService.addReply(parentCommentId, content, user);
        return "redirect:/user/community";
    }

    // Edit comment
    @PostMapping("/comment/{commentId}/edit")
    public String editComment(@PathVariable Long commentId, @RequestParam String content,
            @RequestParam(required = false) Long postId, Principal principal, HttpSession session) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        try {
            communityService.updateComment(commentId, content, user);
            session.setAttribute("succMsg", "Comment updated!");
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }
        if (postId != null) {
            return "redirect:/user/community/post/" + postId;
        }
        return "redirect:/user/community";
    }

    // Delete comment
    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
            @RequestParam(required = false) Long postId, Principal principal, HttpSession session) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        try {
            communityService.deleteComment(commentId, user);
            session.setAttribute("succMsg", "Comment deleted!");
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }
        if (postId != null) {
            return "redirect:/user/community/post/" + postId;
        }
        return "redirect:/user/community";
    }

    // Helper class for JSON response
    static class LikeResponse {
        public boolean liked;
        public Long likeCount;

        public LikeResponse(boolean liked, Long likeCount) {
            this.liked = liked;
            this.likeCount = likeCount;
        }
    }
}
