package com.example.community.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    public Member register(String username, String rawPassword, String nickname) {

        // 1) 아이디 중복 체크
        if (memberRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 2) (원하면 여기서 비밀번호 길이·조합 검사도 넣을 수 있음)
        // 여기서는 일단 그대로 사용

        String encoded = passwordEncoder.encode(rawPassword);
        Member member = new Member(username, encoded, nickname);
        member.setPasswordChangedAt(LocalDateTime.now());
        return memberRepository.save(member);
    }


    // username으로 회원 조회
    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
    }

    // 닉네임 변경
    public void updateNickname(String username, String newNickname) {
        Member member = findByUsername(username);
        member.setNickname(newNickname);
        memberRepository.save(member);
    }

    // 비밀번호 변경 (검증 포함)
    public String changePassword(String username,
                                 String currentPassword,
                                 String newPassword,
                                 String confirmPassword) {

        Member member = findByUsername(username);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            return "현재 비밀번호가 일치하지 않습니다.";
        }

        // 새 비밀번호 / 확인 일치
        if (!newPassword.equals(confirmPassword)) {
            return "새 비밀번호와 확인이 일치하지 않습니다.";
        }

        // 아이디/닉네임과 동일 금지
        if (newPassword.equals(member.getUsername()) || newPassword.equals(member.getNickname())) {
            return "아이디 또는 닉네임과 동일한 비밀번호는 사용할 수 없습니다.";
        }

        // 길이 8~20
        if (newPassword.length() < 8 || newPassword.length() > 20) {
            return "비밀번호는 8~20자여야 합니다.";
        }

        // 영문 / 숫자 / 특수문자 조합 여부
        boolean hasLetter = Pattern.compile("[A-Za-z]").matcher(newPassword).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(newPassword).find();
        boolean hasSpecial = Pattern.compile("[^A-Za-z0-9]").matcher(newPassword).find();

        if (!(hasLetter && hasDigit) && !(hasLetter && hasSpecial) && !(hasDigit && hasSpecial)) {
            return "영문, 숫자, 특수문자 중 두 가지 이상을 조합해 주세요.";
        }

        // 동일 문자·숫자 3자리 연속 금지 (aaa, 111, !!!)
        if (Pattern.compile("(.)\\1\\1").matcher(newPassword).find()) {
            return "동일 문자를 3자리 이상 연속으로 사용할 수 없습니다. (예: 111, aaa, !!!)";
        }

        // 연속 증가 3자리 (123, abc 등) 제한 – 간단 체크
        if (hasSequentialTriplet(newPassword)) {
            return "연속된 문자/숫자 3자리는 사용할 수 없습니다. (예: 123, abc)";
        }

        // 모든 검증 통과 → 비번 변경
        member.setPassword(passwordEncoder.encode(newPassword));
        member.setPasswordChangedAt(LocalDateTime.now());
        memberRepository.save(member);

        return null; // null이면 성공
    }

    // 비밀번호 변경 후 경과일 계산
    public long getDaysSincePasswordChanged(Member member) {
        if (member.getPasswordChangedAt() == null) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(member.getPasswordChangedAt(), LocalDateTime.now());
    }

    // 연속 3글자(숫자/알파벳) 체크
    private boolean hasSequentialTriplet(String password) {
        String lower = password.toLowerCase();
        for (int i = 0; i <= lower.length() - 3; i++) {
            char a = lower.charAt(i);
            char b = lower.charAt(i + 1);
            char c = lower.charAt(i + 2);

            // 모두 숫자
            if (Character.isDigit(a) && Character.isDigit(b) && Character.isDigit(c)) {
                if ((b == a + 1) && (c == b + 1)) return true;
            }

            // 모두 알파벳
            if (Character.isLetter(a) && Character.isLetter(b) && Character.isLetter(c)) {
                if ((b == a + 1) && (c == b + 1)) return true;
            }
        }
        return false;
    }
}
