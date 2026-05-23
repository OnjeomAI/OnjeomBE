package com.onjeom.backend.domain.problem.dto.request;

import com.onjeom.backend.domain.problem.enums.ProblemType;
import com.onjeom.backend.domain.problem.enums.ReadingType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateProblemRequest(
        @NotBlank String passageText,
        @NotBlank String questionText,
        @NotNull ProblemType problemType,
        @NotNull ReadingType readingType,
        @NotNull @Min(1) @Max(5) Integer difficulty,
        @NotBlank String modelAnswer,
        @Size(max = 10) List<ProblemKeywordRequest> keywords
) {}
