package com.twincle.auth_practice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // 스프링에게 이건 서버 켜질 때 제일 먼저 읽어야 하는 설정 파일임을 알려줌
@EnableWebSecurity // 스프링 시큐리티 작동 시작
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // JWT를 쓸 때는 보통 CSRF 방어 기능을 끕니다.
                .authorizeHttpRequests(auth -> auth
                        // 스프링 시큐리티에게 /api/auth/ 로 시작하는 주소는 신분증 없이 일단 무조건 통과시키라고 명령 (로그인/가입 창구니까)
                        .requestMatchers("/api/auth/**").permitAll()
                        // 그 외의 모든 주소는 무조건 JWT가 있는지 검사
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    // 비밀번호를 '1234' 그대로 두지 않고 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}