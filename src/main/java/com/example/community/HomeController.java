package com.example.community;

import com.example.community.board.BoardService;
import com.example.community.member.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final BoardService boardService;

    public HomeController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model) {

        model.addAttribute("boards", boardService.getAllBoards());
        model.addAttribute("currentUser", userDetails); // 로그인 정보 (null 가능)
        return "home";
    }
}
