package com.twincle.auth_practice.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// OncePerRequestFilter: 요청을 보낼 때마다 한 번씩만 검사
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider; // TokenProvider 가져오기

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. HTTP 요청 Header에서 'Authorization'를 찾습니다.
        String bearerToken = request.getHeader("Authorization");

        // 2. 'Bearer '라는 암호가 적혀있고, 뒤에 진짜 토큰이 있는지 확인합니다.
        String token = null;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7); // "Bearer " 일곱 글자 딱지 떼고 진짜 쏼라쏼라 토큰만 가져오기!
        }

        // 3. 토큰이 존재하고, TokenProvider로 확인 시 진짜라고 판단한다면
        if (token != null && tokenProvider.validateToken(token)) {
            // 4. TokenProvider에게서 이메일 정보를 꺼내와서, 우리 서버의 '현재 명부(SecurityContext)'에 이름 적어두기!
            Authentication authentication = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 5. 내(JwtFilter) 검사는 끝났으니, 다음 관문이나 최종 목적지(Controller)로 통과시켜주기!
        filterChain.doFilter(request, response);
    }
}