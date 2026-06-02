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

배포 서버 Swagger UI: `http://3.37.114.1:8080/swagger-ui/index.html#/`

### AI 서버 URL 설정

```yaml
# application-dev.yml
ai:
  server:
    url: ${AI_SERVER_URL:http://localhost:8000}
```

> Kaggle ngrok 사용 시 `AI_SERVER_URL` 환경변수로 주입  
> 예: `AI_SERVER_URL=https://xxxx.ngrok-free.app ./gradlew bootRun`

---

## 패키지 구조

```
com.onjeom.backend
├── domain/
│   ├── auth/           # 로그인, JWT, OAuth2 (Google, Kakao) — OTP 제거됨
│   ├── user/           # 프로필 관리 (FontSize enum 포함)
│   ├── problem/        # 문제 CRUD
│   ├── cms/            # 관리자 문제 등록, AI 문제 생성, 키워드 관리
│   ├── curriculum/     # 맞춤형 커리큘럼 생성·진행·완료 처리
│   ├── diagnostic/     # 적응형 진단 테스트 (IRT 3PL, AiIrtServiceImpl 연동)
│   ├── response/       # 답변 제출 + AI 채점 + competency_scores 업데이트
│   ├── writing/        # Writing AI 연동 4개 엔드포인트
│   ├── learning/       # 하이라이트, 복습 스케줄, 역량 점수 (BKT)
│   ├── dashboard/      # 학습 현황 대시보드 (⚠️ 서비스 구현 중)
│   ├── notification/   # 알림 (⚠️ 구조만 존재)
│   └── ai/             # AI 서버 연동 인터페이스 및 구현체
│       ├── AiScoringService      → AiScoringServiceWritingImpl (Writing AI, 기본값)
│       ├── AiGradingService      → AiGradingServiceImpl / AiGradingServiceMock
│       ├── AiCurriculumService   → AiCurriculumServiceGenerateImpl / AiCurriculumServiceWritingImpl
│       ├── AiTutorService        → AiTutorServiceImpl
│       ├── AiWritingService      → AiWritingServiceImpl / AiWritingServiceMock
│       └── AiIrtService          → AiIrtServiceImpl / AiIrtServiceMock
└── global/
    ├── common/         # ApiResponse, BaseTimeEntity
    ├── config/         # Security, Swagger, WebMvc, AiApiConfig
    ├── exception/      # GlobalExceptionHandler, ErrorCode
    └── security/       # JWT, OAuth2 (Google, Kakao)
```

---

## AI 서버 연동

OnjeomAI (FastAPI) 서버와 HTTP (RestClient)로 통신합니다.

| BE 엔드포인트 | AI 엔드포인트 | 상태 |
|-------------|-------------|------|
| `POST /api/responses` (내부) | `POST /api/writing/evaluate` | ✅ |
| `POST /api/ai/tutor` | `POST /api/tutor/ask` | ✅ |
| `POST /api/ai/explain` | `POST /api/writing/explain-term` | ✅ |
| `POST /api/admin/cms/problems/generate` | `POST /api/problems/generate` + `/api/indexing/index` | ✅ |
| 진단 완료 시 자동 | `POST /api/curriculum/generate` | ✅ |
| 진단 submit 시 내부 | `POST /api/writing/irt/estimate` | ✅ |
| `POST /api/writing/evaluate` | `POST /api/writing/evaluate` | ✅ |
| `POST /api/writing/curriculum/adjust` | `POST /api/writing/curriculum/adjust` | ✅ |
| `POST /api/writing/compare` | `POST /api/writing/compare` | ✅ |
| `POST /api/writing/weakness-report` | `POST /api/writing/weakness-report` | ✅ |

> **채점 기본값**: `AiScoringServiceWritingImpl` (`matchIfMissing=true`) — 진단·학습 모두 Writing AI (Llama 8B) 사용

---

## 도메인별 구현 현황

| 도메인 | 상태 | 비고 |
|--------|------|------|
| auth | ✅ 완료 | JWT 로그인/회원가입, OAuth2 (Google, Kakao), OTP 제거됨 |
| user | ✅ 완료 | 프로필 관리 |
| problem | ✅ 완료 | CRUD |
| cms | ✅ 완료 | AI 문제 생성 + 키워드 관리 |
| curriculum | ✅ 완료 | 생성, 진행, 완료 처리 |
| diagnostic | ✅ 완료 | IRT 3PL 기반 진단 테스트 |
| response | ✅ 완료 | 답변 제출 + AI 채점 + competency 업데이트 |
| writing | ✅ 완료 | Writing AI 연동 4개 엔드포인트 |
| learning | ✅ 완료 | 하이라이트, 복습 스케줄, 역량 점수 |
| dashboard | ⚠️ 진행 중 | DTO 정의됨, 서비스 로직 구현 필요 |
| notification | ⚠️ 미완성 | 구조만 존재 |

---

## 개발 컨벤션

- **DTO**: Java Record 타입, `request/` `response/` 패키지 분리
- **Controller**: `ResponseEntity<ApiResponse<?>>` 반환
- **Swagger**: Controller 직접 작성 금지 → `controller/api/XxxApi.java` 인터페이스에 작성
- **예외처리**: `ErrorCode` enum 정의 → `GlobalExceptionHandler` 통합 처리

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
