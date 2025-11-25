package com.example.community.member;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // ===== 회원가입 폼 =====
    @GetMapping("/members/new")
    public String showJoinForm(Model model) {
        model.addAttribute("errorMessage", null);
        return "members/join";   // templates/members/join.html
    }

    // ===== 회원가입 처리 =====
    @PostMapping("/members/new")
    public String join(@RequestParam String username,
                       @RequestParam String password,
                       @RequestParam String passwordConfirm,
                       @RequestParam String nickname,
                       @RequestParam(value = "termsAgree", required = false) String termsAgree,
                       @RequestParam(value = "privacyAgree", required = false) String privacyAgree,
                       Model model) {

        // 1) 약관 동의 체크
        if (termsAgree == null || privacyAgree == null) {
            model.addAttribute("errorMessage", "필수 약관에 모두 동의해 주세요.");
            return "members/join";
        }

        // 2) 비밀번호 확인 일치 여부
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("errorMessage", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return "members/join";
        }

        // 3) 실제 회원가입 + 예외 처리
        try {
            memberService.register(username, password, nickname);
        } catch (IllegalArgumentException e) {      // 아이디 중복 등 비즈니스 예외
            model.addAttribute("errorMessage", e.getMessage());
            return "members/join";
        } catch (Exception e) {                     // 기타 예외
            model.addAttribute("errorMessage", "회원가입 중 오류가 발생했습니다.");
            return "members/join";
        }

        // ✅ 회원가입 성공 시 홈으로 이동
        return "redirect:/";
    }

    // ===== 계정 설정 화면 =====
    @GetMapping("/account/settings")
    public String accountSettings(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Member member = memberService.findByUsername(userDetails.getUsername());
        long days = memberService.getDaysSincePasswordChanged(member);

        model.addAttribute("member", member);
        model.addAttribute("passwordDays", days);
        model.addAttribute("nicknameMessage", null);
        model.addAttribute("passwordMessage", null);

        return "members/account-settings";
    }

    // ===== 닉네임 변경 =====
    @PostMapping("/account/nickname")
    public String changeNickname(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam String nickname,
                                Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        // 1) DB 에 닉네임 업데이트
        memberService.updateNickname(userDetails.getUsername(), nickname);

        // 2) 최신 Member 다시 조회
        Member member = memberService.findByUsername(userDetails.getUsername());
        long days = memberService.getDaysSincePasswordChanged(member);

        // 3) 🔄 SecurityContext 의 principal 갱신 (그래야 홈에서도 새 닉네임 보임)
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails newPrincipal = new CustomUserDetails(member);

        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(
                        newPrincipal,
                        currentAuth.getCredentials(),
                        currentAuth.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(newAuth);

        // ✅ 바로 홈으로 보내기
        return "redirect:/";
    }


    // ===== 비밀번호 변경 =====
    @PostMapping("/account/password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        String error = memberService.changePassword(
                userDetails.getUsername(),
                currentPassword,
                newPassword,
                confirmPassword
        );

        Member member = memberService.findByUsername(userDetails.getUsername());
        long days = memberService.getDaysSincePasswordChanged(member);

        model.addAttribute("member", member);
        model.addAttribute("passwordDays", days);

        if (error != null) {
            model.addAttribute("passwordMessage", error);
        } else {
            model.addAttribute("passwordMessage", "비밀번호가 변경되었습니다.");
        }
        model.addAttribute("nicknameMessage", null);

        return "members/account-settings";
    }
}
