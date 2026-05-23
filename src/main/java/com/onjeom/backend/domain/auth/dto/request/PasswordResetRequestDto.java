package com.onjeom.backend.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestDto(
        @Email @NotBlank String email
) {}
