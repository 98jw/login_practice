package com.twincle.auth_practice.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component // 스프링에게 이 TokenProvider를 Bean에 등록하고 관리하라고 명령
public class TokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    // application.properties에 적어둔 비밀키와 시간들을 가져와서 TokenProvider 세팅
    public TokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity-in-milliseconds}") long accessTokenValidityTime,
            @Value("${jwt.refresh-token-validity-in-milliseconds}") long refreshTokenValidityTime) {

        // 사용자가 설정한 비밀번호를 SecretKey로 변환하는 과정
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);

        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
    }

    // ---------------------------------------------------------
    // 1. Access Token 만들기 (30분짜리)
    // ---------------------------------------------------------
    public String createAccessToken(String email, UUID tenantId) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.accessTokenValidityTime);

        return Jwts.builder()
                .subject(email) // 사용자 이름(이메일) 적기
                .claim("tenantId", tenantId.toString()) // 추가 정보(UUID) 적어주기
                .signWith(key) // 비밀키 도입 (위조 방지 핵심)
                .expiration(validity) // 만료 시간(30분 뒤) 적기
                .compact(); // 압축해서 문자열로 반환
    }

    // ---------------------------------------------------------
    // 2. Refresh Token 만들기 (7일짜리)
    // ---------------------------------------------------------
    public String createRefreshToken(String email) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.refreshTokenValidityTime);

        // Refresh Token은 굳이 무겁게 UUID 같은 걸 안 달고, 딱 이메일만 넣어서 가볍게 만듭니다.
        return Jwts.builder()
                .subject(email)
                .signWith(key)
                .expiration(validity)
                .compact();
    }
}