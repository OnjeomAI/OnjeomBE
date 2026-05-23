package com.onjeom.backend.domain.diagnostic.dto.response;

import java.math.BigDecimal;

public record DiagnosticResultResponse(
        Long diagnosticId,
        BigDecimal theta,
        Integer factualScore,
        Integer inferentialScore,
        Integer criticalScore,
        Integer vocabularyScore,
        Integer logicalScore,
        Integer level,
        Long curriculumId
) {
}
