package com.example.community.post;

import com.example.community.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 특정 게시판의 글 목록 (최신 글이 위로)
    List<Post> findByBoardOrderByIdDesc(Board board);
}
