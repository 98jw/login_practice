package com.twincle.auth_practice.repository;

import com.twincle.auth_practice.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

// JpaRepository<다룰 엔티티, 그 엔티티의 PK 타입>을 상속받으면 기본 세팅 끝
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    // 로그인 로직을 짤 때 "이메일로 사용자 찾기" 기능이 무조건 필요하겠죠?
    // 이 한 줄만 적어두면 스프링 부트가 알아서 SQL(SELECT 문)을 만들어줍니다
    Optional<Tenant> findByLoginEmail(String loginEmail);

}