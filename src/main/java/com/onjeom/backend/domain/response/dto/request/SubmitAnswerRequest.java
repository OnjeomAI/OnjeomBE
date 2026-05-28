package com.onjeom.backend.domain.response.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitAnswerRequest(
        @NotNull Long problemId,
        @NotBlank String answerText,
        @NotNull @Min(0) Integer responseTimeSec,
        Long curriculumItemId  // nullable
) {}
