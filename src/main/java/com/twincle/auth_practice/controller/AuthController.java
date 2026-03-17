package com.twincle.auth_practice.controller;

import com.twincle.auth_practice.service.AuthService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/setup")
    public String setup() {
        authService.createDummyUser();
        return "테스트 계정(test@test.com / 1234)이 DB에 성공적으로 만들어졌습니다!";
    }

    // 반환 타입 : AuthService.TokenDto
    @PostMapping("/login")
    public AuthService.TokenDto login(@RequestBody LoginRequestDto request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @Getter
    public static class LoginRequestDto {
        private String email;
        private String password;
    }
}