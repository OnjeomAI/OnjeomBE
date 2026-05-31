-- ================================================================
-- OnjeomBE — MySQL 8.0 DDL
-- Generated from JPA entity analysis
-- Naming: Spring PhysicalNamingStrategy (camelCase → snake_case)
-- ================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ================================================================
-- 1. users
--    Entity: User extends BaseTimeEntity
-- ================================================================
CREATE TABLE users (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    email          VARCHAR(255) NOT NULL,
    password       VARCHAR(255) NOT NULL,
    nickname       VARCHAR(50)  NOT NULL,
    role           VARCHAR(20)  NOT NULL COMMENT 'GUEST | STUDENT | ADMIN',
    daily_goal     INT          NOT NULL,
    email_verified TINYINT(1)   NOT NULL,
    alarm_enabled  TINYINT(1)   NOT NULL,
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 2. problems
--    Entity: Problem extends BaseTimeEntity
--    3PL IRT params (a/b/c) nullable → 1PL fallback 가능
-- ================================================================
CREATE TABLE problems (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    passage_text         TEXT         NOT NULL,
    question_text        TEXT         NOT NULL,
    problem_type         VARCHAR(20)  NOT NULL COMMENT 'MULTIPLE_CHOICE | SHORT_ANSWER',
    reading_type         VARCHAR(20)  NOT NULL COMMENT 'FACTUAL | INFERENTIAL | CRITICAL | CREATIVE',
    difficulty           INT          NOT NULL,
    a_param              DECIMAL(5,3)          COMMENT '3PL 변별도 파라미터 (nullable)',
    b_param              DECIMAL(5,3)          COMMENT '3PL 난이도 파라미터 (nullable)',
    c_param              DECIMAL(5,3)          COMMENT '3PL 추측도 파라미터 (nullable)',
    model_answer         TEXT         NOT NULL,
    vector_indexed       TINYINT(1)   NOT NULL,
    vector_index_status  VARCHAR(20)  NOT NULL COMMENT 'PENDING | INDEXING | DONE',
    created_at           DATETIME(6),
    updated_at           DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_problems_difficulty    (difficulty),
    INDEX idx_problems_reading_type  (reading_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 3. problem_choices
--    Entity: ProblemChoice (No BaseTimeEntity)
-- ================================================================
CREATE TABLE problem_choices (
    id             BIGINT     NOT NULL AUTO_INCREMENT,
    problem_id     BIGINT     NOT NULL,
    choice_number  INT        NOT NULL,
    choice_text    TEXT       NOT NULL,
    is_correct     TINYINT(1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_problem_choices_problem
        FOREIGN KEY (problem_id) REFERENCES problems (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 4. problem_keywords
--    Entity: ProblemKeyword (No BaseTimeEntity)
-- ================================================================
CREATE TABLE problem_keywords (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    problem_id  BIGINT       NOT NULL,
    keyword     VARCHAR(100) NOT NULL,
    weight      INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_problem_keywords_problem
        FOREIGN KEY (problem_id) REFERENCES problems (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 5. refresh_tokens
--    Entity: RefreshToken extends BaseTimeEntity
--    Note: userId는 @ManyToOne 없이 Long 저장 (JPA FK 없음)
-- ================================================================
CREATE TABLE refresh_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,
    device_info VARCHAR(255),
    expires_at  DATETIME(6)  NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_refresh_tokens_token_hash (token_hash),
    INDEX idx_refresh_tokens_user_id    (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 6. token_blacklist
--    Entity: TokenBlacklist (No BaseTimeEntity)
--    Note: userId는 @ManyToOne 없이 Long 저장 (JPA FK 없음)
-- ================================================================
CREATE TABLE token_blacklist (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    token_hash      VARCHAR(255) NOT NULL,
    reason          VARCHAR(30)  NOT NULL COMMENT 'LOGOUT | FORCE_LOGOUT | PASSWORD_RESET',
    invalidated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_token_blacklist_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_token_blacklist_token_hash (token_hash),
    INDEX idx_token_blacklist_user_id    (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 7. diagnostic_results
--    Entity: DiagnosticResult extends BaseTimeEntity
--    theta: DECIMAL(5,3) → 범위 -99.999 ~ 99.999 (실제 -3.0 ~ 3.0)
-- ================================================================
CREATE TABLE diagnostic_results (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    theta             DECIMAL(5,3) NOT NULL COMMENT 'IRT 능력 추정치 (-3.0 ~ 3.0)',
    factual_score     INT          NOT NULL,
    inferential_score INT          NOT NULL,
    critical_score    INT          NOT NULL,
    vocabulary_score  INT          NOT NULL,
    logical_score     INT          NOT NULL,
    level             INT          NOT NULL COMMENT '1=하 | 2=중 | 3=상',
    created_at        DATETIME(6),
    updated_at        DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_diagnostic_results_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_diagnostic_results_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 8. curricula
--    Entity: Curriculum extends BaseTimeEntity
-- ================================================================
CREATE TABLE curricula (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    user_id        BIGINT      NOT NULL,
    diagnostic_id  BIGINT      NOT NULL,
    status         VARCHAR(20) NOT NULL COMMENT 'ACTIVE | PAUSED | COMPLETED',
    current_stage  INT         NOT NULL,
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_curricula_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_curricula_diagnostic
        FOREIGN KEY (diagnostic_id) REFERENCES diagnostic_results (id),
    INDEX idx_curricula_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 9. curriculum_items
--    Entity: CurriculumItem extends BaseTimeEntity
-- ================================================================
CREATE TABLE curriculum_items (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    curriculum_id  BIGINT      NOT NULL,
    problem_id     BIGINT      NOT NULL,
    stage          INT         NOT NULL,
    order_index    INT         NOT NULL,
    status         VARCHAR(20) NOT NULL COMMENT 'PENDING | IN_PROGRESS | COMPLETED | SKIPPED',
    scheduled_at   DATETIME(6),
    completed_at   DATETIME(6),
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_curriculum_items_curriculum
        FOREIGN KEY (curriculum_id) REFERENCES curricula (id) ON DELETE CASCADE,
    CONSTRAINT fk_curriculum_items_problem
        FOREIGN KEY (problem_id) REFERENCES problems (id),
    INDEX idx_curriculum_items_curriculum_id (curriculum_id),
    INDEX idx_curriculum_items_stage         (curriculum_id, stage, order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 10. responses
--     Entity: Response (No BaseTimeEntity — @CreatedDate만 존재)
--     curriculum_item_id: nullable Long, JPA @ManyToOne 없음
-- ================================================================
CREATE TABLE responses (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    user_id             BIGINT      NOT NULL,
    problem_id          BIGINT      NOT NULL,
    curriculum_item_id  BIGINT               COMMENT 'ref: curriculum_items.id (JPA FK 없음)',
    answer_text         TEXT        NOT NULL,
    response_time_sec   INT         NOT NULL,
    raw_score           INT         NOT NULL,
    final_score         INT         NOT NULL,
    feedback_text       TEXT,
    error_type          VARCHAR(50),
    scoring_basis       TEXT,
    attempt_number      INT         NOT NULL,
    created_at          DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_responses_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_responses_problem
        FOREIGN KEY (problem_id) REFERENCES problems (id),
    INDEX idx_responses_user_id      (user_id),
    INDEX idx_responses_problem_id   (problem_id),
    INDEX idx_responses_user_problem (user_id, problem_id),
    INDEX idx_responses_created_at   (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 11. highlights
--     Entity: Highlight extends BaseTimeEntity
-- ================================================================
CREATE TABLE highlights (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    user_id       BIGINT      NOT NULL,
    problem_id    BIGINT      NOT NULL,
    start_offset  INT         NOT NULL,
    end_offset    INT         NOT NULL,
    color         VARCHAR(10) NOT NULL COMMENT 'YELLOW | BLUE | PINK',
    created_at    DATETIME(6),
    updated_at    DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_highlights_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_highlights_problem
        FOREIGN KEY (problem_id) REFERENCES problems (id) ON DELETE CASCADE,
    INDEX idx_highlights_user_problem (user_id, problem_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 12. knowledge_tracing
--     Entity: KnowledgeTracing (No BaseTimeEntity — updated_at 수동 관리)
--     DECIMAL(5,4): 정수부 1자리, 소수부 4자리 → 0.0000 ~ 0.9999
--     (user_id, competency_type) UNIQUE: 유저당 역량 1행
-- ================================================================
CREATE TABLE knowledge_tracing (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    competency_type VARCHAR(20)  NOT NULL COMMENT 'FACTUAL | INFERENTIAL | CRITICAL | VOCABULARY | LOGICAL',
    p_know          DECIMAL(5,4) NOT NULL COMMENT 'P(학습됨) — BKT 현재 상태',
    p_learn         DECIMAL(5,4) NOT NULL COMMENT 'P(전이) — 고정 파라미터',
    p_slip          DECIMAL(5,4) NOT NULL COMMENT 'P(실수) — 고정 파라미터',
    p_guess         DECIMAL(5,4) NOT NULL COMMENT 'P(추측) — 고정 파라미터',
    updated_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_knowledge_tracing_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE KEY uq_knowledge_tracing_user_type (user_id, competency_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 13. competency_scores
--     Entity: CompetencyScore (No BaseTimeEntity — measured_at 수동 관리)
--     시계열 데이터: 유저+역량 조합으로 다수 행 허용
--     level: 한국어 ENUM값 (초급|중급|고급|심화) → VARCHAR(10) utf8mb4
-- ================================================================
CREATE TABLE competency_scores (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    competency_type VARCHAR(20)  NOT NULL COMMENT 'FACTUAL | INFERENTIAL | CRITICAL | VOCABULARY | LOGICAL',
    score           DECIMAL(5,2) NOT NULL COMMENT '0.00 ~ 100.00 (pKnow × 100)',
    level           VARCHAR(10)  NOT NULL COMMENT '초급 | 중급 | 고급 | 심화',
    delta           DECIMAL(5,2) NOT NULL COMMENT '직전 측정 대비 점수 변화량',
    measured_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_competency_scores_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_competency_scores_user_type_time (user_id, competency_type, measured_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 14. review_schedules
--     Entity: ReviewSchedule (No BaseTimeEntity)
--     (user_id, problem_id) UNIQUE: 문제당 복습 스케줄 1행
--     에빙하우스 간격: {1, 3, 7, 14, 30}일
-- ================================================================
CREATE TABLE review_schedules (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    user_id          BIGINT      NOT NULL,
    problem_id       BIGINT      NOT NULL,
    review_count     INT         NOT NULL,
    next_review_at   DATETIME(6) NOT NULL,
    last_reviewed_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_review_schedules_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_schedules_problem
        FOREIGN KEY (problem_id) REFERENCES problems (id),
    UNIQUE KEY uq_review_schedules_user_problem (user_id, problem_id),
    INDEX idx_review_schedules_due (user_id, next_review_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
