package com.twincle.auth_practice.controller;

import com.twincle.auth_practice.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    public String signup(@Valid @RequestBody SignupRequestDto request) {
        return authService.signup(request.getEmail(), request.getPassword(), request.getTenantName());
    }

    // 프론트엔드가 가입할 때 보낼 데이터를 담을 새로운 포장지(DTO)
    @Getter
    public static class SignupRequestDto {

        @NotBlank(message = "이메일은 반드시 입력해야 합니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다. (예: test@test.com)")
        private String email;

        @NotBlank(message = "비밀번호는 반드시 입력해야 합니다.")
        @Size(min = 4, message = "비밀번호는 4글자 이상으로 설정해 주세요.")
        private String password;

        @NotBlank(message = "기업 이름은 반드시 입력해야 합니다.")
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