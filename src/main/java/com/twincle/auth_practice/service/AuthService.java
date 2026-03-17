package com.twincle.auth_practice.service;

import com.twincle.auth_practice.domain.Tenant;
import com.twincle.auth_practice.jwt.TokenProvider;
import com.twincle.auth_practice.repository.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final TokenProvider tokenProvider; // 우리가 만든 TokenProvider 가져오기

    // 1. 임시 테스트용 계정 만들기
    @Transactional
    public void createDummyUser() {
        if (tenantRepository.findByLoginEmail("test@test.com").isPresent()) {
            return;
        }

        Tenant dummyUser = Tenant.builder()
                .tenantCode("TC_001")
                .tenantName("제욱님의 테스트 기업")
                .loginEmail("test@test.com")
                .passwordHash("1234")
                .status("ACTIVE")
                .refreshTokenHash("임시토큰")
                .build();

        tenantRepository.save(dummyUser);
    }

    // 2. 로그인 로직 (토큰 발급 로직 추가)
    @Transactional(readOnly = true)
    public TokenDto login(String email, String password) {

        // ① 이메일 검사
        Tenant tenant = tenantRepository.findByLoginEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // ② 비밀번호 검사 (나중에 암호화 로직으로 바꿀 부분입니다!)
        if (!tenant.getPasswordHash().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // ③ 검사 통과! 공장장에게 30분짜리 Access 토큰, 7일짜리 Refresh 토큰 만들어달라고 명령
        String accessToken = tokenProvider.createAccessToken(tenant.getLoginEmail(), tenant.getTenantId());
        String refreshToken = tokenProvider.createRefreshToken(tenant.getLoginEmail());

        // ④ 프론트엔드가 알아보기 쉽게 포장지(TokenDto)에 담아서 돌려주기
        return new TokenDto(accessToken, refreshToken);
    }

    // 토큰 2개를 담아서 보내줄 포장지(DTO) 클래스
    @Getter
    @AllArgsConstructor
    public static class TokenDto {
        private String accessToken;
        private String refreshToken;
    }
}