package com.example.community.member;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        // src/main/resources/templates/login.html 를 의미
        return "login";
    }
}
