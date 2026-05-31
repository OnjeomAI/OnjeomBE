package com.onjeom.backend.domain.writing.dto.response;

import java.util.List;

public record WeaknessReportResponse(
        List<WeakCompetencyDetail> weakCompetencies,
        String report,
        List<String> recommendations,
        String priorityCompetency
) {}
