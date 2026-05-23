package com.onjeom.backend.domain.user.dto.response;

public record UserProfileResponse(
        Long userId,
        String email,
        String nickname,
        String role,
        Integer dailyGoal,
        Boolean alarmEnabled,
        Boolean emailVerified
) {}
