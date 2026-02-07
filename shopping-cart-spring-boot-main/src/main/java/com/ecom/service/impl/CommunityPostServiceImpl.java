package com.ecom.service.impl;

import com.ecom.model.CommunityPost;
import com.ecom.repository.CommunityPostRepository;
import com.ecom.repository.NotificationRepository;
import com.ecom.repository.PetCommentRepository;
import com.ecom.repository.PostLikeRepository;
import com.ecom.service.CommunityPostService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class CommunityPostServiceImpl implements CommunityPostService {

    @Autowired
    private CommunityPostRepository communityPostRepository;

    @Autowired
    private PetCommentRepository petCommentRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    @Transactional
    public boolean deletePostWithDependencies(Long postId) {
        try {
            CommunityPost post = communityPostRepository.findById(postId).orElse(null);
            if (post == null) {
                return false;
            }

            // ลบ comments ก่อน
            petCommentRepository.deleteByPostId(postId);

            // ลบ likes
            postLikeRepository.deleteByPost(post);

            // ลบ notifications ที่เกี่ยวข้อง
            notificationRepository.deleteByPost(post);

            // ลบ post สุดท้าย
            communityPostRepository.delete(post);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
