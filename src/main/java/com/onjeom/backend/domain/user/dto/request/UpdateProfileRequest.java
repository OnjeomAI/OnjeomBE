package com.onjeom.backend.domain.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 50) String nickname,
        @NotNull Boolean alarmEnabled,
        @NotNull @Min(5) @Max(20) Integer dailyGoal
) {}
