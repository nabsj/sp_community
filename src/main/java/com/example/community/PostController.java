package com.example.community.post;

import com.example.community.board.Board;
import com.example.community.member.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/boards/{boardId}")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 특정 커뮤니티의 글 목록
    @GetMapping
    public String list(@PathVariable Long boardId, Model model) {
        Board board = postService.getBoard(boardId);
        List<Post> posts = postService.getPostListByBoard(boardId);

        model.addAttribute("board", board);
        model.addAttribute("posts", posts);
        return "posts/list";
    }

    // 글 작성 폼
    @GetMapping("/posts/new")
    public String showCreateForm(@PathVariable Long boardId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 HttpServletRequest request,
                                 Model model) {

        Board board = postService.getBoard(boardId);
        model.addAttribute("board", board);

        String writerDisplay = buildWriter(userDetails, request);
        model.addAttribute("writerDisplay", writerDisplay);

        return "posts/form";
    }

    // 글 작성 처리
    @PostMapping("/posts")
    public String createPost(@PathVariable Long boardId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             HttpServletRequest request,
                             @RequestParam String title,
                             @RequestParam String content) {

        String writer = buildWriter(userDetails, request);
        postService.createPost(boardId, title, content, writer);
        return "redirect:/boards/" + boardId;
    }

    // 글 상세보기 (조회수 +1)
    @GetMapping("/posts/{postId}")
    public String detail(@PathVariable Long boardId,
                         @PathVariable Long postId,
                         Model model) {

        Board board = postService.getBoard(boardId);
        Post post = postService.viewPost(postId);

        model.addAttribute("board", board);
        model.addAttribute("post", post);
        return "posts/detail";
    }

    // ----------------- private helper -----------------

    private String buildWriter(CustomUserDetails userDetails, HttpServletRequest request) {
        // 로그인 된 경우 → 닉네임 사용
        if (userDetails != null) {
            return userDetails.getNickname();
        }

        // 익명인 경우 → 익명(220. 78) 형태
        String ip = request.getRemoteAddr();  // 예: 220.78.38.321
        String masked = maskIp(ip);
        return "익명(" + masked + ")";
    }

    private String maskIp(String ip) {
        if (ip == null) {
            return "알수없음";
        }
        String[] parts = ip.split("\\.");
        if (parts.length >= 2) {
            // 예: 220.78.38.321 → "220. 78"
            return parts[0] + ". " + parts[1];
        }
        return ip;
    }
}
