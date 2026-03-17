package com.twincle.auth_practice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity // 스프링에게 이 클래스를 보고 DB에 테이블을 만들어달라는 라는 뜻
@Table(name = "tenants") // 실제 DB에 만들어질 테이블 이름
@Getter // 숨겨진 변수들을 꺼내볼 수 있는 메서드를 자동 생성 (Lombok 마법)
@NoArgsConstructor // 텅 빈 기본 생성자를 자동 생성 (Lombok 마법)
@AllArgsConstructor // 모든 재료를 받는 생성자 자동 생성
@Builder            // 레고 블록처럼 객체를 쉽게 조립하게 해주는 도구
public class Tenant {

    @Id // 이 컬럼이 PK 역할을 한다는 뜻
    @GeneratedValue(strategy = GenerationType.UUID) // UUID를 서버가 자동으로 생성
    @Column(name = "tenant_id", updatable = false, nullable = false)
    private UUID tenantId;

    @Column(name = "tenant_code", length = 64, nullable = false)
    private String tenantCode;

    @Column(name = "tenant_name", length = 128, nullable = false)
    private String tenantName;

    @Column(name = "login_email", length = 255, nullable = false, unique = true)
    private String loginEmail; // 이메일은 중복 가입을 막기 위해 unique=true 설정

    @Column(name = "password_hash", columnDefinition = "TEXT", nullable = false)
    private String passwordHash;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @Column(name = "last_login_at")
    private ZonedDateTime lastLoginAt; // PostgreSQL의 TIMESTAMPTZ와 어울리는타입

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "refresh_token_hash", columnDefinition = "TEXT", nullable = false)
    private String refreshTokenHash;

    // 데이터가 처음 DB에 저장될 때, 현재 시간을 자동으로 입력
    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    // 데이터가 수정될 때, 수정된 시간을 자동으로 업데이트
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }
}