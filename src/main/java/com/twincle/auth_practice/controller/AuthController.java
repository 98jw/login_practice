package com.twincle.auth_practice.controller;

import com.twincle.auth_practice.service.AuthService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController // 여기는 인터넷 요청을 받는 창구(API)라고 선언
@RequestMapping("/api/auth") // 이 창구의 기본 주소는 '/api/auth'로 시작함
@RequiredArgsConstructor
public class AuthController {

    // 창구 직원이 계산을 맡길 두뇌(Service)를 연결
    private final AuthService authService;

    // ---------------------------------------------------------
    // 1. 임시 계정 생성 API (인터넷 창에서 바로 접속하기 위해 GET 방식 사용)
    // ---------------------------------------------------------
    @GetMapping("/setup")
    public String setup() {
        authService.createDummyUser();
        return "테스트 계정(test@test.com / 1234)이 DB에 성공적으로 만들어졌습니다!";
    }

    // ---------------------------------------------------------
    // 2. 로그인 API (보안을 위해 POST 방식을 쓰고, 데이터를 Body에 숨겨서 받습니다)
    // ---------------------------------------------------------
    @PostMapping("/login")
    public String login(@RequestBody LoginRequestDto request) {
        // 창구 직원이 손님이 준 포장지(DTO)에서 이메일/비번을 꺼내서 두뇌로 넘깁니다!
        return authService.login(request.getEmail(), request.getPassword());
    }

    // 프론트엔드가 보내는 데이터를 예쁘게 담을 '포장지(DTO)' 클래스
    @Getter
    public static class LoginRequestDto {
        private String email;
        private String password;
    }
}