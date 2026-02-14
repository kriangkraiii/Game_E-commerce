package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecom.model.Post;
import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.CommunityService;

import org.springframework.data.domain.Page;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/community")
public class AdminCommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String getAllPosts(Model model, Principal principal,
            @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        model.addAttribute("user", user);

        Page<Post> page = communityService.getAllPostsPagination(pageNo, pageSize);
        List<Post> posts = page.getContent();

        model.addAttribute("posts", posts);
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/community";
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
        return "redirect:/admin/community";
    }

    // Edit post page
    @GetMapping("/post/{postId}/edit")
    public String editPostPage(@PathVariable Long postId, Model model, Principal principal) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        model.addAttribute("user", user);

        Post post = communityService.getPostById(postId);
        model.addAttribute("post", post);
        return "admin/edit_post";
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
        return "redirect:/admin/community";
    }

    // Delete comment
    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId, Principal principal, HttpSession session) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        try {
            communityService.deleteComment(commentId, user);
            session.setAttribute("succMsg", "Comment deleted!");
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/community";
    }

    // Edit comment
    @PostMapping("/comment/{commentId}/edit")
    public String editComment(@PathVariable Long commentId, @RequestParam String content,
            Principal principal, HttpSession session) {
        String email = principal.getName();
        UserDtls user = userRepository.findByEmail(email);
        try {
            communityService.updateComment(commentId, content, user);
            session.setAttribute("succMsg", "Comment updated!");
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/community";
    }
}
