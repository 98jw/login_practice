package com.twincle.auth_practice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user") // 여기는 '/api/auth'가 아니기 때문에 무조건 토큰을 검사
public class UserController {

    // 내 정보를 조회하는 API
    @GetMapping("/me")
    public String getMyInfo(Authentication authentication) {
        // 확인한 신분증(Authentication)에서 이름(이메일)을 꺼내서 보여줍니다!
        return "환영합니다! 당신의 이메일은 [" + authentication.getName() + "] 입니다.";
    }
}