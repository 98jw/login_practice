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

    // 회원가입 API
    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequestDto request) {
        return authService.signup(request.getEmail(), request.getPassword(), request.getTenantName());
    }

    // 프론트엔드가 가입할 때 보낼 데이터를 담을 새로운 포장지(DTO)
    @Getter
    public static class SignupRequestDto {
        private String email;
        private String password;
        private String tenantName;
    }

    // 토큰 재발급 API (만료되었을 때 갱신용)
    @PostMapping("/reissue")
    public AuthService.TokenDto reissue(@RequestBody ReissueRequestDto request) {
        // 받은 Refresh Token을 Service로 넘겨서 새 토큰 세트를 받아옵니다!
        return authService.reissue(request.getRefreshToken());
    }

    // 재발급 요청 시 토큰을 담아올 DTO
    @Getter
    public static class ReissueRequestDto {
        private String refreshToken;
    }
}