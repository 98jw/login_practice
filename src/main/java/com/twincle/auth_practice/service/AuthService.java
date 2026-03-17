package com.twincle.auth_practice.service;

import com.twincle.auth_practice.domain.Tenant;
import com.twincle.auth_practice.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // 스프링에게 여기가 비즈니스 로직을 담당하는 뇌(Service) 라고 알려줌
@RequiredArgsConstructor // Repository를 자동으로 롬복이 연결해 줌
public class AuthService {

    // 우리가 아까 만든 그 Repository를 가져옴 (final을 꼭 붙여야 롬복이 연결해 줍니다!)
    private final TenantRepository tenantRepository;

    // ---------------------------------------------------------
    // 1. 임시 테스트용 계정 만들기 (DB가 비어있으니 로그인을 해볼 수가 없어서 만듭니다)
    // ---------------------------------------------------------
    @Transactional
    public void createDummyUser() {
        // 무전기 쳐서 "test@test.com" 있는지 물어봄. 이미 있으면 안 만듦!
        if (tenantRepository.findByLoginEmail("test@test.com").isPresent()) {
            return;
        }

        // 아까 추가한 @Builder 덕분에 이렇게 직관적으로 유저를 만들 수 있습니다.
        Tenant dummyUser = Tenant.builder()
                .tenantCode("TC_001")
                .tenantName("제욱님의 테스트 기업")
                .loginEmail("test@test.com")
                .passwordHash("1234") // 원래는 암호화해야 하지만 일단 날것으로 테스트!
                .status("ACTIVE")
                .refreshTokenHash("임시토큰")
                .build();

        // Repository로 DB에게 이 유저 정보 좀 저장해달라고 명령합니다.
        tenantRepository.save(dummyUser);
    }

    // ---------------------------------------------------------
    // 2. 로그인 로직
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public String login(String email, String password) {

        // ① 무전기 쳐서 이메일로 사용자 찾아오기. (못 찾으면 에러 던지기!)
        Tenant tenant = tenantRepository.findByLoginEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // ② DB에서 꺼내온 비밀번호와, 방금 입력한 비밀번호가 똑같은지 비교하기
        if (!tenant.getPasswordHash().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // ③ 둘 다 통과했다면? 성공 메시지 반환!
        // (나중에는 이 부분에 "Access/Refresh JWT 토큰 발급" 코드가 들어갑니다!)
        return "로그인 성공! 환영합니다, " + tenant.getTenantName() + "님!";
    }
}