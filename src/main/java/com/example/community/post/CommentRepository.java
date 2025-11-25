package com.example.community.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글별 댓글 목록 (오래된 순)
    List<Comment> findByPostOrderByIdAsc(Post post);
}
