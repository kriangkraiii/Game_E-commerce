package com.ecom.service;

import com.ecom.model.PetPost;
import com.ecom.model.PetPostComment;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PetPostService {

    Page<PetPost> getAllPosts(int pageNo, int pageSize);

    long getLikeCountByPostId(Integer postId);

    List<PetPostComment> getCommentsByPostId(Integer postId);

    boolean deletePost(Integer postId);
}
