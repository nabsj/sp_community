package com.example.community.board;

import com.example.community.post.Post;
import com.example.community.post.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;
    private final PostService postService;

    public BoardController(BoardService boardService,
                           PostService postService) {
        this.boardService = boardService;
        this.postService = postService;
    }

    /**
     * 게시판별 글 목록 화면
     * GET /boards/{boardId}
     */
    @GetMapping("/{boardId}")
    public String viewBoard(@PathVariable("boardId") Long boardId,
                            Model model) {

        // 1) 게시판 정보 조회 (ex. 갤러리, 볼링, 유머...)
        Board board = boardService.getBoard(boardId);

        // 2) 해당 게시판의 글 목록 조회 (최신 글 순)
        List<Post> posts = postService.getPostsByBoard(boardId);

        // 3) 템플릿에 전달
        model.addAttribute("board", board);
        model.addAttribute("posts", posts);

        // templates/posts/list.html
        return "posts/list";
    }
}
