# 온점 백엔드 API 명세서

## 공통 사항

### Base URL
```
http://localhost:8080
```

### 인증
JWT Bearer Token 방식 사용

```
Authorization: Bearer {accessToken}
```

인증이 필요한 API는 헤더에 토큰을 포함해야 합니다.

### 공통 응답 형식

```json
{
  "success": true,
  "message": "OK",
  "data": { ... }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| success | boolean | 성공 여부 |
| message | String | 응답 메시지 |
| data | T | 응답 데이터 (실패 시 null) |

### Enum 타입

| Enum | 값 |
|------|-----|
| ReadingType | `FACTUAL`, `INFERENTIAL`, `CRITICAL`, `CREATIVE` |
| ProblemType | `WRITING` |
| CurriculumStatus | `IN_PROGRESS`, `COMPLETED`, `PAUSED` |
| CurriculumItemStatus | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `SKIPPED` |
| HighlightColor | `YELLOW`, `GREEN`, `BLUE`, `PINK` |
| CompetencyType | `FACTUAL`, `INFERENTIAL`, `CRITICAL`, `VOCABULARY`, `LOGICAL` |
| CompetencyLevel | `LOW`, `MEDIUM`, `HIGH` |
| VectorIndexStatus | `PENDING`, `DONE`, `FAILED` |
| FontSize | `SMALL`, `MEDIUM`, `LARGE` |

---

## 1. 인증 (Auth)

### 1-1. 회원가입

**POST** `/api/auth/signup`

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "닉네임"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | String | O | 이메일 형식 |
| password | String | O | 최소 8자 |
| nickname | String | O | 최대 50자 |

**Response**
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다. 이메일을 확인해주세요.",
  "data": null
}
```

---

### 1-2. 이메일 인증

**POST** `/api/auth/email/verify`

**Request Body**
```json
{
  "email": "user@example.com",
  "otpCode": "123456"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | String | O | 이메일 형식 |
| otpCode | String | O | 6자리 인증 코드 |

**Response**
```json
{
  "success": true,
  "message": "이메일 인증이 완료되었습니다.",
  "data": null
}
```

---

### 1-3. 로그인

**POST** `/api/auth/login`

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "닉네임",
    "role": "ROLE_USER",
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci..."
  }
}
```

---

### 1-4. 토큰 재발급

**POST** `/api/auth/token/reissue`

**Request Header**
```
Authorization: Bearer {refreshToken}
```

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci..."
  }
}
```

---

### 1-5. 로그아웃

**POST** `/api/auth/logout` `🔒 인증 필요`

**Request Header**
```
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "message": "로그아웃되었습니다.",
  "data": null
}
```

---

### 1-6. 전체 기기 로그아웃

**POST** `/api/auth/logout/all` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "전체 기기에서 로그아웃되었습니다.",
  "data": null
}
```

---

### 1-7. 비밀번호 재설정 요청

**POST** `/api/auth/password/reset-request`

**Request Body**
```json
{
  "email": "user@example.com"
}
```

**Response**
```json
{
  "success": true,
  "message": "비밀번호 재설정 이메일을 발송했습니다.",
  "data": null
}
```

---

### 1-8. 비밀번호 재설정

**POST** `/api/auth/password/reset`

**Request Body**
```json
{
  "token": "reset-token",
  "newPassword": "newPassword123"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| token | String | O | 이메일로 받은 재설정 토큰 |
| newPassword | String | O | 최소 8자 |

**Response**
```json
{
  "success": true,
  "message": "비밀번호가 변경되었습니다.",
  "data": null
}
```

---

## 2. 사용자 (User)

### 2-1. 내 프로필 조회

**GET** `/api/users/me` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "닉네임",
    "role": "ROLE_USER",
    "dailyGoal": 10,
    "alarmEnabled": true,
    "emailVerified": true,
    "fontSize": "MEDIUM"
  }
}
```

---

### 2-2. 프로필 수정

**PUT** `/api/users/me` `🔒 인증 필요`

**Request Body**
```json
{
  "nickname": "새닉네임",
  "alarmEnabled": true,
  "dailyGoal": 10,
  "fontSize": "MEDIUM"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| nickname | String | O | 최대 50자 |
| alarmEnabled | Boolean | O | 알람 활성화 여부 |
| dailyGoal | Integer | O | 일일 목표 (5~20) |
| fontSize | FontSize | X | `SMALL` / `MEDIUM` / `LARGE` (기본값 MEDIUM) |

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "새닉네임",
    "role": "ROLE_USER",
    "dailyGoal": 10,
    "alarmEnabled": true,
    "emailVerified": true,
    "fontSize": "MEDIUM"
  }
}
```

---

## 3. 문제 (Problem)

### 3-1. 문제 목록 조회

**GET** `/api/problems` `🔒 인증 필요`

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| page | Integer | 0 | 페이지 번호 |
| size | Integer | 20 | 페이지 크기 |

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "questionText": "다음 글의 중심 내용은?",
      "problemType": "WRITING",
      "readingType": "FACTUAL",
      "difficulty": 3,
      "vectorIndexStatus": "DONE",
      "createdAt": "2026-05-30T10:00:00"
    }
  ]
}
```

---

### 3-2. 문제 상세 조회

**GET** `/api/problems/{problemId}` `🔒 인증 필요`

**Path Variables**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| problemId | Long | 문제 ID |

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "passageText": "지문 내용...",
    "questionText": "다음 글의 중심 내용은?",
    "problemType": "WRITING",
    "readingType": "FACTUAL",
    "difficulty": 3,
    "modelAnswer": "모범 답안...",
    "vectorIndexed": true,
    "vectorIndexStatus": "DONE",
    "keywords": [
      { "keyword": "핵심어", "weight": 10 }
    ],
    "createdAt": "2026-05-30T10:00:00"
  }
}
```

---

### 3-3. 독서 유형별 문제 조회

**GET** `/api/problems/type/{readingType}` `🔒 인증 필요`

**Path Variables**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| readingType | ReadingType | `FACTUAL` / `INFERENTIAL` / `CRITICAL` / `CREATIVE` |

**Response** - 3-1과 동일한 배열 형식

---

## 4. 진단 (Diagnostic)

### 4-1. 진단 시작

**POST** `/api/diagnostic/start` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "diagnosticId": 1,
    "problemId": 10,
    "passageText": "지문...",
    "questionText": "문제..."
  }
}
```

---

### 4-2. 진단 답변 제출

**POST** `/api/diagnostic/submit` `🔒 인증 필요`

**Request Body**
```json
{
  "problemId": 10,
  "answerText": "학생 답변...",
  "responseTimeSec": 120
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| problemId | Long | O | 문제 ID |
| answerText | String | O | 답변 내용 |
| responseTimeSec | Integer | O | 응답 시간(초) |

**Response**
- 진단 계속: 다음 문제 정보 반환
- 진단 완료: `data: null` (결과 조회 API로 확인)

---

### 4-3. 최신 진단 결과 조회

**GET** `/api/diagnostic/result` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "diagnosticId": 1,
    "theta": 0.5,
    "factualScore": 75,
    "inferentialScore": 60,
    "criticalScore": 55,
    "vocabularyScore": 80,
    "logicalScore": 70,
    "level": 3,
    "curriculumId": 1
  }
}
```

---

## 5. 답변 제출 (Response)

### 5-1. 답변 제출

**POST** `/api/responses` `🔒 인증 필요`

**Request Body**
```json
{
  "problemId": 1,
  "answerText": "학생 답변 내용...",
  "responseTimeSec": 180,
  "curriculumItemId": 10
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| problemId | Long | O | 문제 ID |
| answerText | String | O | 답변 내용 |
| responseTimeSec | Integer | O | 응답 시간(초, 0 이상) |
| curriculumItemId | Long | X | 커리큘럼 항목 ID |

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "problemId": 1,
    "answerText": "학생 답변 내용...",
    "rawScore": 70,
    "finalScore": 75,
    "feedbackText": "피드백 내용...",
    "scoringBasis": "EXCELLENCE",
    "foundKeywords": ["핵심어1", "핵심어2"],
    "attemptNumber": 1,
    "createdAt": "2026-05-30T10:00:00"
  }
}
```

---

### 5-2. 응답 단건 조회

**GET** `/api/responses/{responseId}` `🔒 인증 필요`

**Response** - 5-1 응답과 동일

---

### 5-3. 문제별 응답 조회

**GET** `/api/responses/problem/{problemId}` `🔒 인증 필요`

**Response** - 5-1 응답과 동일한 배열 형식

---

### 5-4. 내 답변 vs 모범답안 비교

**GET** `/api/responses/{responseId}/compare` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "responseId": 1,
    "problemId": 1,
    "passageText": "지문 전체 내용...",
    "questionText": "다음 글의 중심 내용은?",
    "studentAnswer": "학생 답변 내용...",
    "modelAnswer": "모범 답안 내용...",
    "finalScore": 75,
    "feedbackText": "AI 피드백 내용...",
    "keywords": [
      { "id": 1, "keyword": "핵심어", "weight": 10 }
    ]
  }
}
```

---

## 6. 커리큘럼 (Curriculum)

### 6-1. 내 커리큘럼 조회

**GET** `/api/curriculum/me` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "curriculumId": 1,
    "status": "IN_PROGRESS",
    "currentStage": 2,
    "totalItems": 70,
    "completedItems": 15,
    "todayItems": [
      {
        "itemId": 1,
        "problemId": 10,
        "questionText": "문제 내용...",
        "readingType": "FACTUAL",
        "difficulty": 3,
        "stage": 2,
        "orderIndex": 1,
        "status": "PENDING",
        "scheduledAt": "2026-05-30T09:00:00"
      }
    ]
  }
}
```

---

### 6-2. 커리큘럼 진행 상황 조회

**GET** `/api/curriculum/progress` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "curriculumId": 1,
    "currentStage": 2,
    "totalItems": 70,
    "completedItems": 15,
    "skippedItems": 2,
    "progressPercent": 21.43
  }
}
```

---

### 6-3. 커리큘럼 항목 시작

**PATCH** `/api/curriculum/items/{itemId}/start` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": null
}
```

---

### 6-4. 커리큘럼 항목 건너뛰기

**PATCH** `/api/curriculum/items/{itemId}/skip` `🔒 인증 필요`

**Response** - 6-3과 동일

---

### 6-5. 커리큘럼 항목 완료

**PATCH** `/api/curriculum/items/{itemId}/complete` `🔒 인증 필요`

**Response** - 6-3과 동일

---

## 7. 대시보드 (Dashboard)

### 7-1. 레이더 차트 조회

**GET** `/api/dashboard/radar` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "competencies": [
      {
        "type": "FACTUAL",
        "score": 75.0,
        "level": "HIGH",
        "delta": 5.0
      }
    ]
  }
}
```

---

### 7-2. 학습 통계 조회

**GET** `/api/dashboard/stats` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "totalResponses": 42,
    "averageScore": 72.5,
    "streakDays": 5,
    "recentStats": [
      {
        "date": "2026-05-30",
        "count": 5,
        "averageScore": 78.0
      }
    ]
  }
}
```

---

### 7-3. 오늘의 목표 조회

**GET** `/api/dashboard/today` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "dailyGoal": 10,
    "completedToday": 4,
    "goalAchieved": false,
    "dueReviews": [
      {
        "scheduleId": 1,
        "problemId": 5,
        "questionText": "문제 내용...",
        "readingType": "INFERENTIAL",
        "reviewCount": 2,
        "nextReviewAt": "2026-05-30T10:00:00",
        "lastReviewedAt": "2026-05-28T10:00:00"
      }
    ]
  }
}
```

---

### 7-4. 최근 응답 조회

**GET** `/api/dashboard/recent-responses` `🔒 인증 필요`

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| page | Integer | 0 | 페이지 번호 |
| size | Integer | 10 | 페이지 크기 |

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "responses": [
      {
        "responseId": 1,
        "problemId": 10,
        "questionText": "문제 내용...",
        "readingType": "FACTUAL",
        "finalScore": 80,
        "createdAt": "2026-05-30T10:00:00"
      }
    ],
    "totalCount": 42,
    "currentPage": 0,
    "totalPages": 5
  }
}
```

---

### 7-5. 약점 리포트 조회

**GET** `/api/dashboard/weak-points` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "weakCompetencies": [
      {
        "type": "CRITICAL",
        "score": 45.0,
        "level": "LOW"
      }
    ],
    "reviewDueCount": 3
  }
}
```

---

## 8. AI 글쓰기 보조 (Writing)

### 8-1. 커리큘럼 동적 재조정

**POST** `/api/writing/curriculum/adjust` `🔒 인증 필요`

**Request Body**
```json
{
  "competencyHistory": [
    {
      "competencyType": "FACTUAL",
      "scores": [80, 75, 45]
    }
  ]
}
```

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "needsAdjustment": true,
    "weakCompetencies": ["CRITICAL"],
    "adjustmentMessage": "비판적 이해 역량이 3회 연속 50점 미만입니다.",
    "recommendedFocus": "비판적 이해"
  }
}
```

---

### 8-2. 답변 비교 (성장 추적)

**POST** `/api/writing/compare` `🔒 인증 필요`

**Request Body**
```json
{
  "problemId": 1,
  "previousAnswer": "이전 답변...",
  "previousScore": 60,
  "currentAnswer": "현재 답변...",
  "currentScore": 75
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| problemId | Long | O | 문제 ID |
| previousAnswer | String | O | 이전 답변 |
| previousScore | Integer | O | 이전 점수 (0~100) |
| currentAnswer | String | O | 현재 답변 |
| currentScore | Integer | O | 현재 점수 (0~100) |

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "scoreDiff": 15,
    "isImproved": true,
    "growthMessage": "이전보다 15점 향상되었습니다!",
    "newlyIncludedKeywords": ["핵심어A"],
    "stillMissingKeywords": ["핵심어B"],
    "analysis": "논리 흐름이 개선되었으나..."
  }
}
```

---

### 8-3. 약점 분석 리포트 생성

**POST** `/api/writing/weakness-report` `🔒 인증 필요`

**Request Body**
```json
{
  "competencyScores": [
    {
      "competencyType": "FACTUAL",
      "averageScore": 75.0
    }
  ]
}
```

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "weakCompetencies": [
      {
        "competencyType": "CRITICAL",
        "averageScore": 42.0,
        "level": "취약"
      }
    ],
    "report": "비판적 이해 역량에서 약점이 발견되었습니다...",
    "recommendations": ["관련 지문 반복 학습 권장"],
    "priorityCompetency": "CRITICAL"
  }
}
```

---

## 9. AI 튜터 (AI Tutor)

### 9-1. AI 튜터 질문

**POST** `/api/ai/tutor` `🔒 인증 필요`

**Request Body**
```json
{
  "question": "이 지문에서 주제를 찾는 방법이 뭔가요?",
  "problemId": 1,
  "passageText": "지문 내용..."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| question | String | O | 질문 내용 |
| problemId | Long | X | 관련 문제 ID |
| passageText | String | X | 지문 텍스트 |

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "answer": "주제를 찾을 때는...",
    "references": ["참고자료1", "참고자료2"]
  }
}
```

---

### 9-2. 용어 설명 (도움 기능)

**POST** `/api/ai/explain` `🔒 인증 필요`

드래그 선택하거나 직접 입력한 용어/문장을 AI가 쉬운 말로 설명합니다. `passageText` 제공 시 지문 맥락을 반영한 설명을 생성합니다.

**Request Body**
```json
{
  "term": "주제",
  "passageText": "지문 내용..."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| term | String | O | 설명이 필요한 용어 또는 문장 |
| passageText | String | X | 지문 텍스트 (맥락 기반 설명에 활용) |

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "term": "주제",
    "explanation": "글쓴이가 글을 통해 전달하려는 핵심 내용을 말해요..."
  }
}
```

---

## 10. 학습 도구 (Learning)

### 10-1. 하이라이트 저장

**POST** `/api/highlights` `🔒 인증 필요`

**Request Body**
```json
{
  "problemId": 1,
  "startOffset": 10,
  "endOffset": 50,
  "color": "YELLOW"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| problemId | Long | O | 문제 ID |
| startOffset | Integer | O | 시작 오프셋 |
| endOffset | Integer | O | 끝 오프셋 |
| color | HighlightColor | O | `YELLOW` / `GREEN` / `BLUE` / `PINK` |

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "problemId": 1,
    "startOffset": 10,
    "endOffset": 50,
    "color": "YELLOW",
    "createdAt": "2026-05-30T10:00:00"
  }
}
```

---

### 10-2. 하이라이트 목록 조회

**GET** `/api/highlights/{problemId}` `🔒 인증 필요`

**Response** - 10-1 응답과 동일한 배열 형식

---

### 10-3. 하이라이트 삭제

**DELETE** `/api/highlights/{problemId}` `🔒 인증 필요`

**Query Parameters**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| startOffset | Integer | 시작 오프셋 |
| endOffset | Integer | 끝 오프셋 |

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": null
}
```

---

### 10-4. 오늘의 복습 조회

**GET** `/api/review/today` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "scheduleId": 1,
      "problemId": 5,
      "questionText": "문제 내용...",
      "readingType": "INFERENTIAL",
      "reviewCount": 2,
      "nextReviewAt": "2026-05-30T10:00:00",
      "lastReviewedAt": "2026-05-28T10:00:00"
    }
  ]
}
```

---

### 10-5. 전체 복습 스케줄 조회

**GET** `/api/review/all` `🔒 인증 필요`

**Response** - 10-4와 동일한 배열 형식

---

## 11. 알림 (Notification)

### 11-1. 알림 조회

**GET** `/api/notifications` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "notifications": [
      {
        "type": "REVIEW_DUE",
        "title": "복습 알림",
        "message": "오늘 복습할 문제가 있습니다.",
        "createdAt": "2026-05-30T09:00:00"
      }
    ]
  }
}
```

---

## 12. 관리자 CMS (Admin)

> 관리자 권한(`ROLE_ADMIN`) 필요

### 12-1. 전체 문제 조회

**GET** `/api/admin/cms/problems` `🔒 인증 필요`

**Query Parameters**

| 파라미터 | 타입 | 기본값 |
|---------|------|--------|
| page | Integer | 0 |
| size | Integer | 20 |

---

### 12-2. 문제 생성

**POST** `/api/admin/cms/problems` `🔒 인증 필요`

**Request Body**
```json
{
  "passageText": "지문 내용...",
  "questionText": "문제 내용...",
  "problemType": "WRITING",
  "readingType": "FACTUAL",
  "difficulty": 3,
  "modelAnswer": "모범 답안...",
  "keywords": [
    { "keyword": "핵심어", "weight": 10 }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| passageText | String | O | 지문 |
| questionText | String | O | 문제 |
| problemType | ProblemType | O | `WRITING` |
| readingType | ReadingType | O | 독서 유형 |
| difficulty | Integer | O | 난이도 (1~5) |
| modelAnswer | String | O | 모범 답안 |
| keywords | List | X | 키워드 목록 (최대 10개) |

---

### 12-3. AI 문제 생성

**POST** `/api/admin/cms/problems/generate` `🔒 인증 필요`

**Request Body**
```json
{
  "difficulty": 3,
  "readingType": "INFERENTIAL",
  "topic": "환경"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| difficulty | Integer | O | 난이도 (1~5) |
| readingType | ReadingType | O | 독서 유형 |
| topic | String | X | 주제 |

---

### 12-4. 문제 수정

**PATCH** `/api/admin/cms/problems/{problemId}` `🔒 인증 필요`

**Request Body** (모든 필드 선택)
```json
{
  "passageText": "수정된 지문...",
  "questionText": "수정된 문제...",
  "readingType": "CRITICAL",
  "difficulty": 4,
  "modelAnswer": "수정된 모범 답안..."
}
```

---

### 12-5. 문제 삭제

**DELETE** `/api/admin/cms/problems/{problemId}` `🔒 인증 필요`

---

### 12-6. 키워드 수정

**PUT** `/api/admin/cms/problems/{problemId}/keywords` `🔒 인증 필요`

**Request Body**
```json
{
  "keywords": [
    { "keyword": "핵심어", "weight": 10 }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| keywords | List | 키워드 목록 (1~10개) |

---

### 12-7. 커리큘럼 문제 순서 편집

**PUT** `/api/admin/cms/curriculum/{curriculumId}/order` `🔒 인증 필요`

드래그 앤 드롭으로 변경된 순서를 저장합니다. `problemIds` 배열의 순서대로 `orderIndex`가 재설정됩니다.

**Request Body**
```json
{
  "problemIds": [3, 1, 5, 2, 4]
}
```

**Response**
```json
{
  "success": true,
  "message": "커리큘럼 순서가 업데이트되었습니다.",
  "data": null
}
```

---

### 12-8. 벡터 인덱싱 재시도

**POST** `/api/admin/cms/problems/{problemId}/reindex` `🔒 인증 필요`

PENDING 또는 실패 상태인 문제의 벡터 인덱싱을 수동으로 재시도합니다.

**Response**
```json
{
  "success": true,
  "message": "벡터 인덱싱이 완료되었습니다.",
  "data": null
}
```

---

### 12-9. 관리자 통계 CSV 내보내기

**GET** `/api/admin/dashboard/stats/export` `🔒 인증 필요`

전체 사용자 통계 및 역량별 평균 점수를 CSV 파일로 다운로드합니다.

- Content-Type: `text/csv; charset=UTF-8`
- Content-Disposition: `attachment; filename="onjeom-stats.csv"`

---

### 12-10. 관리자 통계 조회

**GET** `/api/admin/dashboard/stats` `🔒 인증 필요`

**Response**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "totalUsers": 120,
    "newUsersThisMonth": 15,
    "totalResponses": 3400,
    "activeUsersThisMonth": 80,
    "overallAverageScore": 68.5,
    "competencyStats": [
      {
        "type": "CRITICAL",
        "averageScore": 52.3,
        "userCount": 95
      }
    ]
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| totalUsers | Long | 전체 가입자 수 |
| newUsersThisMonth | Long | 이번 달 신규 가입자 수 |
| totalResponses | Long | 전체 답변 제출 수 |
| activeUsersThisMonth | Long | 이번 달 활성 사용자 수 |
| overallAverageScore | Double | 전체 평균 점수 |
| competencyStats | List | 역량별 통계 |
| competencyStats[].type | CompetencyType | 역량 유형 |
| competencyStats[].averageScore | Double | 역량별 평균 점수 |
| competencyStats[].userCount | Long | 해당 역량 응답 사용자 수 |
