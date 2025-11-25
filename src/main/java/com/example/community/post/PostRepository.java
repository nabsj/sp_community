package com.example.community.post;

import com.example.community.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시판별 최신 글 목록
    List<Post> findByBoardOrderByIdDesc(Board board);
}
