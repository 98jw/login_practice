package com.twincle.auth_practice.controller;

import com.twincle.auth_practice.service.AuthService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    // 1. 내 정보 조회
    @GetMapping("/me")
    public String getMyInfo(Authentication authentication) {
        return "🎉 환영합니다! VIP 구역에 무사히 들어오셨군요. 당신의 이메일은 [" + authentication.getName() + "] 입니다.";
    }

    // 2. 내 정보 수정 (PATCH: 일부 정보만 수정)
    @PatchMapping("/me")
    public String updateMyInfo(Authentication authentication, @RequestBody UpdateRequestDto request) {
        // 토큰에서 꺼낸 내 이메일(getName)과, 새로 바꿀 이름을 넘깁니다!
        return authService.updateTenantName(authentication.getName(), request.getNewName());
    }

    // 3. 회원 탈퇴 (DELETE: 삭제)
    @DeleteMapping("/me")
    public String deleteMyAccount(Authentication authentication) {
        // 토큰에서 꺼낸 내 이메일을 넘겨서 삭제해버립니다!
        return authService.deleteTenant(authentication.getName());
    }

    // 이름 변경 시 사용할 DTO
    @Getter
    public static class UpdateRequestDto {
        private String newName;
    }
}