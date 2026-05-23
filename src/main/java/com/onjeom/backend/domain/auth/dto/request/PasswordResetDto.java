package com.onjeom.backend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetDto(
        @NotBlank String token,
        @NotBlank @Size(min = 8) String newPassword
) {}
