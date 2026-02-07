package com.ecom.service.impl;

import com.ecom.model.PetPost;
import com.ecom.model.PetPostComment;
import com.ecom.repository.PetPostCommentRepository;
import com.ecom.repository.PetPostLikeRepository;
import com.ecom.repository.PetPostRepository;
import com.ecom.service.PetPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class PetPostServiceImpl implements PetPostService {

	@Autowired
	private PetPostRepository petPostRepository;

	@Autowired
	private PetPostCommentRepository commentRepository;

	@Autowired
	private PetPostLikeRepository likeRepository;

	@Override
	public Page<PetPost> getAllPosts(int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo, Math.min(pageSize, 50), Sort.by(Sort.Direction.DESC, "id") // ใช้ id
																												// แทน
																												// createdAt
																												// เพื่อเลี่ยง
																												// error
		);
		return petPostRepository.findAll(pageable);
	}

	@Override
	public long getLikeCountByPostId(Integer postId) {
		return likeRepository.countByPostId(postId);
	}

	@Override
	public List<PetPostComment> getCommentsByPostId(Integer postId) {
		return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
	}

	@Override
	public boolean deletePost(Integer postId) {
		try {
			PetPost post = petPostRepository.findById(postId).orElse(null);
			if (post == null)
				return false;

			// ลบ likes และ comments ที่เกี่ยวข้องก่อน
			likeRepository.deleteByPostId(postId);
			commentRepository.deleteByPostId(postId);

			// ลบไฟล์รูปถ้ามี (และไม่ใช่ default)
			String imageUrl = post.getImageUrl();
			if (imageUrl != null && !imageUrl.isBlank() && imageUrl.startsWith("/img/")
					&& !imageUrl.equals("/img/pet_img/default.jpg")) {
				Path realPath = Paths.get("src/main/resources/static" + imageUrl);
				Files.deleteIfExists(realPath);
			}

			// ลบโพสต์
			petPostRepository.delete(post);

			return true;
		} catch (Exception e) {
			e.printStackTrace(); // ควรใช้ logger แทนใน production
			return false;
		}
	}
}
