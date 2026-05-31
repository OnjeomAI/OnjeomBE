package com.onjeom.backend.domain.writing.dto.response;

import java.util.List;

public record CurriculumAdjustResponse(
        boolean needsAdjustment,
        List<String> weakCompetencies,
        String adjustmentMessage,
        String recommendedFocus
) {}
