package com.example.community.post;

import com.example.community.board.Board;
import com.example.community.board.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;

    public PostService(PostRepository postRepository,
                       BoardRepository boardRepository) {
        this.postRepository = postRepository;
        this.boardRepository = boardRepository;
    }

    public List<Post> getPostListByBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시판을 찾을 수 없습니다. id=" + boardId));
        return postRepository.findByBoardOrderByIdDesc(board);
    }

    public Board getBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시판을 찾을 수 없습니다. id=" + boardId));
    }

    public Post createPost(Long boardId, String title, String content, String writer) {
        Board board = getBoard(boardId);
        Post post = new Post(board, title, content, writer);
        return postRepository.save(post);
    }

    @Transactional
    public Post viewPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + postId));
        post.setViewCount(post.getViewCount() + 1);   // 조회수 +1
        return post;
    }
}
