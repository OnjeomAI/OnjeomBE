package com.onjeom.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT_VALUE(400, "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(405, "허용되지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(415, "지원하지 않는 Content-Type입니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다."),

    // 인증/인가
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 Refresh Token입니다."),

    // 회원
    USER_NOT_FOUND(404, "존재하지 않는 사용자입니다."),
    EMAIL_ALREADY_EXISTS(409, "이미 사용중인 이메일입니다."),
    INVALID_PASSWORD(401, "비밀번호가 올바르지 않습니다."),
    EMAIL_NOT_VERIFIED(403, "이메일 인증이 완료되지 않았습니다."),
    INVALID_OTP(401, "유효하지 않은 인증 코드입니다."),
    EXPIRED_OTP(401, "만료된 인증 코드입니다."),

    // 문제
    PROBLEM_NOT_FOUND(404, "존재하지 않는 문제입니다."),

    // 커리큘럼
    CURRICULUM_NOT_FOUND(404, "존재하지 않는 커리큘럼입니다."),
    CURRICULUM_ALREADY_EXISTS(409, "이미 활성화된 커리큘럼이 존재합니다."),

    // 진단
    DIAGNOSTIC_NOT_FOUND(404, "진단 결과가 존재하지 않습니다."),
    DIAGNOSTIC_SESSION_NOT_FOUND(404, "진단 세션이 존재하지 않습니다."),
    DIAGNOSTIC_ALREADY_COMPLETED(409, "이미 완료된 진단입니다."),
    NO_PROBLEM_AVAILABLE(404, "출제 가능한 문제가 없습니다."),

    // CMS
    KEYWORD_LIMIT_EXCEEDED(400, "핵심 키워드는 문제당 최대 10개까지 등록 가능합니다."),
    INVALID_DIFFICULTY(400, "난이도는 1~5 사이의 값이어야 합니다.");

    private final int status;
    private final String message;
}
