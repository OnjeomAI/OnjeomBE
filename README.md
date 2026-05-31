# OnjeomBE

온점 : 세상을 온전히 읽는 힘 — 백엔드 (Spring Boot)

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 3.x |
| 보안 | Spring Security + JWT |
| DB | MySQL 8.0 |
| ORM | Spring Data JPA (Hibernate) |
| 빌드 | Gradle |
| 문서 | Springdoc OpenAPI (Swagger) |
| 인프라 | AWS EC2, Docker |

---

## 시작하기

### 사전 준비

```bash
# MySQL 컨테이너 실행 (포트 13306)
docker compose up -d
```

### 서버 실행

```bash
./gradlew bootRun
```

서버 기동 후 Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## API 문서 (배포 서버)

**Swagger UI**: http://3.37.114.1:8080/swagger-ui/index.html#/

---

## 브랜치 전략

```
main          # 배포용 (AWS EC2 반영)
develop       # 통합 테스트용
feat/*        # 기능 개발
fix/*         # 버그 수정
refactor/*    # 리팩토링
```

- `feat/*` → PR → `develop` → 테스트 완료 후 → PR → `main`
- `main` 직접 푸시 금지

---

## 패키지 구조

```
com.onjeom.backend
├── domain/
│   ├── auth/           # 로그인, JWT, 이메일 OTP, OAuth2
│   ├── user/           # 프로필 관리
│   ├── problem/        # 문제 CRUD
│   ├── cms/            # 관리자 문제 등록, AI 문제 생성, 키워드 관리
│   ├── curriculum/     # 맞춤형 커리큘럼
│   ├── diagnostic/     # 적응형 진단 테스트 (IRT 2PL)
│   ├── learning/       # 하이라이트, 지식 추적, 복습 스케줄
│   ├── response/       # 답변 제출 및 채점
│   ├── dashboard/      # 학습 현황 대시보드
│   ├── notification/   # 알림
│   └── ai/             # AI 서버 연동 (채점, 커리큘럼, 튜터)
└── global/
    ├── common/         # ApiResponse, BaseTimeEntity
    ├── config/         # Security, Swagger, WebMvc
    ├── exception/      # GlobalExceptionHandler, ErrorCode
    └── security/       # JWT, OAuth2 (Google, Kakao)
```

---

## AI 서버 연동

OnjeomAI (FastAPI) 서버와 HTTP로 통신합니다.

| 서비스 | 엔드포인트 | 상태 |
|--------|-----------|------|
| `AiScoringServiceImpl` | `POST /api/grading/grade` | ✅ 연동 완료 |
| `AiCurriculumServiceImpl` | `POST /api/curriculum/generate` | ✅ 연동 완료 |
| `AiTutorServiceImpl` | `POST /api/tutor/ask` | ✅ 연동 완료 |

AI 서버 URL 설정 (`application-dev.yml`):

```yaml
ai:
  server:
    url: ${AI_SERVER_URL:http://localhost:8000}
```

> Kaggle ngrok 사용 시 `AI_SERVER_URL` 환경변수로 주입

---

## 개발 컨벤션

- **DTO**: Java Record 타입, `request/` `response/` 패키지 분리
- **Controller**: `ResponseEntity<ApiResponse<?>>` 반환
- **Swagger**: Controller 직접 작성 금지 → `controller/api/XxxApi.java` 인터페이스에 작성
- **예외처리**: `ErrorCode` enum 정의 → `GlobalExceptionHandler` 통합 처리
