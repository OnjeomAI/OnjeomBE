package com.onjeom.backend.domain.user.dto.response;

import com.onjeom.backend.domain.user.enums.FontSize;

public record UserProfileResponse(
        Long userId,
        String email,
        String nickname,
        String role,
        Integer dailyGoal,
        Boolean alarmEnabled,
        Boolean emailVerified,
        FontSize fontSize
) {}
