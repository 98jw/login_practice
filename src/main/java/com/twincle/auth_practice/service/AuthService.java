package com.twincle.auth_practice.service;

import com.twincle.auth_practice.domain.Tenant;
import com.twincle.auth_practice.jwt.TokenProvider;
import com.twincle.auth_practice.repository.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final TokenProvider tokenProvider; // 우리가 만든 TokenProvider 가져오기
    private final PasswordEncoder passwordEncoder;

    // 1. 임시 테스트용 계정 만들기
    @Transactional
    public void createDummyUser() {
        if (tenantRepository.findByLoginEmail("test@test.com").isPresent()) {
            return;
        }

        Tenant dummyUser = Tenant.builder()
                .tenantCode("TC_001")
                .tenantName("테스트 아이디")
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

        // ② 비밀번호 검사 (입력받은 비밀번호와 DB의 해싱된 비밀번호를 비교!)
        if (!passwordEncoder.matches(password, tenant.getPasswordHash())) {
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

    // 3. 회원가입 로직
    @Transactional
    public String signup(String email, String password, String tenantName) {
        // ① 이메일 중복 검사 (이미 DB에 있으면 에러 표시)
        if (tenantRepository.findByLoginEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // ② 비밀번호 해싱 (1234 -> $2a$10$w...)
        String encodedPassword = passwordEncoder.encode(password);

        // ③ 새로운 사용자 정보 조립하기
        Tenant newUser = Tenant.builder()
                .tenantCode("TC_" + UUID.randomUUID().toString().substring(0, 5)) // 겹치지 않게 임시 코드 발급
                .tenantName(tenantName)
                .loginEmail(email)
                .passwordHash(encodedPassword) // 해싱된 비밀번호를 DB에 넣음
                .status("ACTIVE")
                .refreshTokenHash("") // 처음 가입할 땐 빈 값으로 둡니다.
                .build();

        // ④ DB에 저장!
        tenantRepository.save(newUser);
        return "회원가입 성공! 환영합니다, " + tenantName + "님!";
    }

    // ---------------------------------------------------------
    // 4. 회원 정보 수정
    // ---------------------------------------------------------
    @Transactional
    public String updateTenantName(String email, String newName) {
        // ① 이메일로 사용자 찾기
        Tenant tenant = tenantRepository.findByLoginEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ② 이름 바꾸기 (JPA가 DB에 UPDATE 쿼리를 날려줌)
        tenant.updateTenantName(newName);

        return "이름이 성공적으로 [" + newName + "](으)로 변경되었습니다!";
    }

    // ---------------------------------------------------------
    // 5. 회원 탈퇴 (DB에서 삭제)
    // ---------------------------------------------------------
    @Transactional
    public String deleteTenant(String email) {
        // ① 이메일로 사용자 찾기
        Tenant tenant = tenantRepository.findByLoginEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ② Repository로 삭제 명령 내리기
        tenantRepository.delete(tenant);

        return "회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.";
    }
}