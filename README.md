# Spring Boot JWT 인증/인가 로직 구현 연습

SSAFY 특화 프로젝트인 twin_cle 프로젝트(바이오 공정 실험 바이오트윈)에서 사용할 사용자 인증 파트의 사전 연습입니다.

따라서 테이블 구조는 실제 프로젝트의 테이블을 가져왔습니다. 구조는 다음과 같습니다.

```bash
CREATE TABLE `tenants` (
	`tenant_id`	UUID	NOT NULL	COMMENT '시스템 내 모든 운영 데이터의 최상위 귀속 주체',
	`tenant_code`	VARCHAR(64)	NOT NULL	COMMENT '고객사 식별 고유 코드',
	`tenant_name`	VARCHAR(128)	NOT NULL	COMMENT '고객사 및 테넌트 이름',
	`login_email`	VARCHAR(255)	NOT NULL	COMMENT '회사 계정 인증에 사용되는 마스터 이메일',
	`password_hash`	TEXT	NOT NULL,
	`status`	VARCHAR(16)	NOT NULL	COMMENT 'ACTIVE (활성) / SUSPENDED (정지)',
	`last_login_at`	TIMESTAMPTZ	NULL,
	`created_at`	TIMESTAMPTZ	NOT NULL	DEFAULT NOW(),
	`updated_at`	TIMESTAMPTZ	NOT NULL	DEFAULT NOW(),
	`refresh_token_hash`	TEXT	NOT NULL	COMMENT '세션 연장 및 검증용 토큰 해시'
);
```

Spring Boot와 Spring Security, 그리고 JWT(JSON Web Token)를 활용하여 **백엔드 사용자 인증 및 인가(Authentication & Authorization)의 전체 사이클을 처음부터 끝까지 직접 구현해 보았습니다.

단순한 로그인 기능 이외에도, 비밀번호 암호화, 토큰 재발급(RTR), 예외 처리, 그리고 프론트엔드와의 협업을 고려한 DTO 검증까지 실무에서 사용하는 핵심 보안 아키텍처를 적용했습니다.

---

## 핵심 내용

1. **DB 설계 및 연결 (PostgreSQL + Spring Data JPA)**
   - `application.properties`를 통한 DB 연동 및 하이버네이트 DDL 자동화 설정
   - `Tenant` 엔티티 생성 및 더티 체킹을 활용한 데이터 수정/삭제
2. **비밀번호 암호화 (BCrypt)**
   - 평문 비밀번호를 DB에 그대로 저장하지 않고, `PasswordEncoder`를 사용해 단방향 해시(Hash) 처리 후 저장 및 검증
3. **JWT 기반 로그인 및 토큰 발급 (Access & Refresh Token)**
   - `io.jsonwebtoken` 라이브러리를 활용한 토큰 생성 공장(`TokenProvider`) 구현
   - 서명(Signature)과 만료 시간(Expiration)이 포함된 안전한 토큰 발급
4. **Spring Security 검문소 구축 (JwtFilter)**
   - 모든 HTTP 요청의 헤더를 검사하여 토큰의 유효성을 돋보기처럼 확인하는 커스텀 필터 구현
   - 인증이 필요 없는 API(가입/로그인)와 인증이 필수인 VIP API(정보 수정/탈퇴)의 철저한 분리
5. **토큰 재발급 자동화 (Refresh Token Rotation)**
   - Access Token 만료 시, DB에 저장된 Refresh Token을 대조하여 새로운 토큰 세트를 안전하게 재발급하는 로직 구현
6. **DTO 데이터 검증 (Validation)**
   - `@Valid`와 `@NotBlank`, `@Email` 등을 활용해 잘못된 형식의 데이터가 서버 두뇌로 침투하는 것을 앞단에서 차단
7. **인텔리제이 HTTP 클라이언트 테스트 자동화**
   - `.http` 파일을 활용해 발급받은 토큰을 글로벌 변수에 자동 저장하고, 복사/붙여넣기 없이 인증 API를 테스트하는 실무 꿀팁 적용

---

## 📁 전체 파일 구조 및 역할 요약

```text
com.twincle.auth_practice
 ┣ 📂 config
 ┃ ┗ 📜 SecurityConfig.java      # [보안 설정 파일] Spring Security의 전체 동작 규칙 설정. 특정 API(가입/로그인) 접근 허용, 커스텀 JwtFilter의 위치 지정, BCrypt 암호화 빈(Bean) 등록 담당.
 ┣ 📂 controller
 ┃ ┣ 📜 AuthController.java      # [인증 API 컨트롤러] 인증(토큰) 없이 접근 가능한 엔드포인트(회원가입, 로그인, 토큰 재발급) 제공. @Valid를 활용한 Request DTO 유효성 검사 수행.
 ┃ ┗ 📜 UserController.java      # [인가 API 컨트롤러] JwtFilter를 통과한(인증된) 사용자만 접근할 수 있는 보호된 엔드포인트(정보 조회, 수정, 탈퇴) 제공.
 ┣ 📂 domain
 ┃ ┗ 📜 Tenant.java              # [JPA 엔티티(Entity)] DB의 'tenants' 테이블과 1:1로 매핑되는 도메인 객체. 데이터 구조 정의 및 필드값(이름, 토큰 등) 업데이트를 위한 비즈니스 메서드 포함.
 ┣ 📂 jwt
 ┃ ┣ 📜 JwtFilter.java           # [JWT 검증 필터] HTTP 요청 헤더에서 토큰을 추출하고, 유효성 및 만료 여부를 검사하여 SecurityContext에 사용자 인증 정보를 저장하는 커스텀 필터.
 ┃ ┗ 📜 TokenProvider.java       # [JWT 유틸리티 클래스] jjwt 라이브러리를 사용하여 Access/Refresh 토큰 생성, 서명 검증, 토큰 내 클레임(Claim) 추출을 전담하는 컴포넌트.
 ┣ 📂 repository
 ┃ ┗ 📜 TenantRepository.java    # [데이터 접근 계층(Repository)] Spring Data JPA를 활용하여 SQL 작성 없이 DB 데이터의 CRUD 연산 및 이메일 기반 사용자 조회(findByLoginEmail) 수행.
 ┣ 📂 service
 ┃ ┗ 📜 AuthService.java         # [비즈니스 로직 계층(Service)] 컨트롤러와 DB 사이에서 핵심 로직 처리. 비밀번호 해싱(BCrypt), 토큰 발급/재발급, 엔티티 상태 변경 등을 트랜잭션(@Transactional) 내에서 수행.
 ┗ 📜 Application.java           # [애플리케이션 진입점] Spring Boot 서버를 구동하는 메인(Main) 실행 파일.

 📂 resources
 ┗ 📜 application.properties     # [환경 설정 파일] DB 접속 정보(URL, 계정명), JPA 하이버네이트 설정(DDL-auto), JWT 시크릿 키 및 만료 시간 등의 전역 환경 변수 정의.

 📂 루트 디렉토리
 ┣ 📜 build.gradle               # [빌드 및 의존성 관리 파일] Spring Security, JWT(jjwt API/Impl/Jackson), Validation, PostgreSQL 드라이버 등 외부 라이브러리 의존성 주입.
 ┗ 📜 test.http                  # [HTTP 테스트 스크립트] 외부 툴(Postman 등) 없이 인텔리제이 내부에서 API를 테스트하는 파일. 로그인 응답 토큰을 환경 변수에 자동 저장하는 스크립트 포함.