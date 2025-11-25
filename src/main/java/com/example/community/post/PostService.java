package com.example.community.post;

import com.example.community.board.Board;
import com.example.community.board.BoardService;
import com.example.community.member.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final BoardService boardService;

    public PostService(PostRepository postRepository, BoardService boardService) {
        this.postRepository = postRepository;
        this.boardService = boardService;
    }

    @Transactional(readOnly = true)
    public List<Post> getPostsByBoard(Long boardId) {
        Board board = boardService.getBoard(boardId);
        return postRepository.findByBoardOrderByIdDesc(board);
    }

    @Transactional
    public Post getPostAndIncreaseViewCount(Long boardId, Long postId) {
        Board board = boardService.getBoard(boardId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다. id=" + postId));

        if (!post.getBoard().getId().equals(board.getId())) {
            throw new IllegalArgumentException("게시판과 게시글 정보가 일치하지 않습니다.");
        }

        post.increaseViewCount();
        return post;
    }

    @Transactional
    public Post createPost(Board board,
                           Member member,
                           String guestNickname,
                           String guestPassword,
                           String title,
                           String content) {

        String writerName = (member != null) ? member.getNickname() : guestNickname;

        Post post = new Post(board, title, content, writerName);
        return postRepository.save(post);
    }
}
