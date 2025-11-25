package com.example.community.global;

import com.example.community.member.Member;
import com.example.community.member.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttribute {

    private final MemberService memberService;

    public GlobalModelAttribute(MemberService memberService) {
        this.memberService = memberService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication) {

        boolean loggedIn = authentication != null && authentication.isAuthenticated();
        model.addAttribute("globalLoggedIn", loggedIn);

        if (!loggedIn) {
            return;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();

            try {
                // username 으로 Member 찾아서 닉네임 사용
                Member member = memberService.findByUsername(username);
                model.addAttribute("globalNickname", member.getNickname());
            } catch (Exception e) {
                // 혹시 Member 못 찾으면 username 이라도 표시
                model.addAttribute("globalNickname", username);
            }
        }
    }
}
