package com.onjeom.backend.domain.auth.dto.response;

public record LoginResponse(
        Long userId,
        String email,
        String nickname,
        String role,
        String accessToken,
        String refreshToken
) {}
