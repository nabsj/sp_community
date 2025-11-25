package com.example.community.post;

import com.example.community.board.Board;
import com.example.community.board.BoardService;
import com.example.community.member.Member;
import com.example.community.member.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/boards/{boardId}/posts")
public class PostController {

    private final PostService postService;
    private final BoardService boardService;
    private final MemberService memberService;

    public PostController(PostService postService,
                          BoardService boardService,
                          MemberService memberService) {
        this.postService = postService;
        this.boardService = boardService;
        this.memberService = memberService;
    }

    /**
     * 글쓰기 폼
     * GET /boards/{boardId}/posts/new
     */
    @GetMapping("/new")
    public String showWriteForm(@PathVariable("boardId") Long boardId,
                                Authentication authentication,
                                Model model) {

        Board board = boardService.getBoard(boardId);
        model.addAttribute("board", board);

        boolean loggedIn = authentication != null && authentication.isAuthenticated();
        model.addAttribute("loggedIn", loggedIn);

        if (loggedIn && authentication.getPrincipal() instanceof UserDetails userDetails) {
            Member member = memberService.findByUsername(userDetails.getUsername());
            model.addAttribute("writerDisplay", member.getNickname());
        } else {
            model.addAttribute("writerDisplay", "손님");
        }

        model.addAttribute("errorMessage", null);

        return "posts/form";
    }

    /**
     * 글 작성 처리
     * POST /boards/{boardId}/posts/new
     */
    @PostMapping("/new")
    public String createPost(@PathVariable("boardId") Long boardId,
                             Authentication authentication,
                             @RequestParam String title,
                             @RequestParam String content,
                             @RequestParam(required = false) String guestNickname,
                             @RequestParam(required = false) String guestPassword,
                             Model model) {

        Board board = boardService.getBoard(boardId);

        boolean loggedIn = authentication != null && authentication.isAuthenticated();
        Member member = null;
        if (loggedIn && authentication.getPrincipal() instanceof UserDetails userDetails) {
            member = memberService.findByUsername(userDetails.getUsername());
        }

        // 제목/내용 필수
        if (title == null || title.trim().isEmpty()
                || content == null || content.trim().isEmpty()) {

            model.addAttribute("board", board);
            model.addAttribute("loggedIn", loggedIn);
            model.addAttribute("writerDisplay", loggedIn && member != null ? member.getNickname() : "손님");
            model.addAttribute("errorMessage", "제목과 내용을 입력해 주세요.");

            return "posts/form";
        }

        // 비로그인 사용자는 닉네임/비번 필수
        if (!loggedIn) {
            if (guestNickname == null || guestNickname.trim().isEmpty()
                    || guestPassword == null || guestPassword.trim().isEmpty()) {

                model.addAttribute("board", board);
                model.addAttribute("loggedIn", false);
                model.addAttribute("writerDisplay", "손님");
                model.addAttribute("errorMessage", "닉네임과 비밀번호를 모두 입력해 주세요.");

                return "posts/form";
            }
        }

        // 실제 저장
        Post post = postService.createPost(
                board,
                member,
                guestNickname,
                guestPassword,
                title,
                content
        );

        return "redirect:/boards/" + boardId + "/posts/" + post.getId();
    }

    /**
     * 글 상세 보기
     * GET /boards/{boardId}/posts/{postId}
     */
    @GetMapping("/{postId}")
    public String viewPost(@PathVariable("boardId") Long boardId,
                           @PathVariable Long postId,
                           Authentication authentication,
                           Model model) {

        Board board = boardService.getBoard(boardId);
        Post post = postService.getPostAndIncreaseViewCount(boardId, postId);

        model.addAttribute("board", board);
        model.addAttribute("post", post);
        model.addAttribute("currentUser", authentication);

        return "posts/detail";
    }
}
