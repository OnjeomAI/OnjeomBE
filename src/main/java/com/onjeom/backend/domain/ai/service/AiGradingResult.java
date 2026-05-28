package com.onjeom.backend.domain.ai.service;

import java.util.List;

public record AiGradingResult(
        int score,
        int stage1Score,
        List<String> foundKeywords,
        List<AiKeyword> missingKeywords,
        String feedback,
        String gradeReason
) {}
