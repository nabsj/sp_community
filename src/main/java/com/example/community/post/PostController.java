package com.example.community.post;

import com.example.community.board.Board;
import com.example.community.board.BoardService;
import com.example.community.member.Member;
import com.example.community.member.MemberService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public String showWriteForm(@PathVariable Long boardId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {

        Board board = boardService.getBoard(boardId);
        model.addAttribute("board", board);

        boolean loggedIn = (userDetails != null);
        model.addAttribute("loggedIn", loggedIn);

        if (loggedIn) {
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
    public String createPost(@PathVariable Long boardId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam String title,
                             @RequestParam String content,
                             @RequestParam(required = false) String guestNickname,
                             @RequestParam(required = false) String guestPassword,
                             Model model) {

        Board board = boardService.getBoard(boardId);

        boolean loggedIn = (userDetails != null);
        Member member = null;
        if (loggedIn) {
            member = memberService.findByUsername(userDetails.getUsername());
        }

        // 기본 검증
        if (title == null || title.trim().isEmpty()
                || content == null || content.trim().isEmpty()) {

            model.addAttribute("board", board);
            model.addAttribute("loggedIn", loggedIn);
            model.addAttribute("writerDisplay", loggedIn ? member.getNickname() : "손님");
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

        // 실제 저장 로직
        String writerName = loggedIn ? member.getNickname() : guestNickname;
        Post post = postService.createPost(
                board,
                writerName,   // 화면에 보이는 작성자 이름
                title,
                content
        );

        // 등록 후 해당 글 상세 페이지로 이동
        return "redirect:/boards/" + boardId + "/posts/" + post.getId();
    }

    /**
     * 글 상세 보기
     * GET /boards/{boardId}/posts/{postId}
     */
    @GetMapping("/{postId}")
    public String viewPost(@PathVariable Long boardId,
                           @PathVariable Long postId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {

        Board board = boardService.getBoard(boardId);
        Post post = postService.getPostAndIncreaseViewCount(boardId, postId);

        boolean loggedIn = (userDetails != null);
        Member member = null;
        boolean alreadyRecommended = false;

        if (loggedIn) {
            try {
                member = memberService.findByUsername(userDetails.getUsername());
                alreadyRecommended = postService.hasUserRecommended(post, member);
            } catch (Exception e) {
                // Member 못 찾거나 에러 나도 그냥 "추천 안 한 걸로" 처리
                alreadyRecommended = false;
            }
        }

        model.addAttribute("board", board);
        model.addAttribute("post", post);
        model.addAttribute("currentUser", userDetails);
        model.addAttribute("alreadyRecommended", alreadyRecommended);

        return "posts/detail";
    }

    /**
     * 추천하기
     * POST /boards/{boardId}/posts/{postId}/recommend
     */
    @PostMapping("/{postId}/recommend")
    public String recommend(@PathVariable Long boardId,
                            @PathVariable Long postId,
                            @AuthenticationPrincipal UserDetails userDetails) {

        // 비회원이면 로그인 화면으로
        if (userDetails == null) {
            return "redirect:/login";
        }

        Post post = postService.getPost(boardId, postId);
        Member member = memberService.findByUsername(userDetails.getUsername());

        postService.recommendPost(post, member);

        return "redirect:/boards/" + boardId + "/posts/" + postId;
    }
}
