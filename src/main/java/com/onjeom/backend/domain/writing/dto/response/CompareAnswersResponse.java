package com.onjeom.backend.domain.writing.dto.response;

import java.util.List;

public record CompareAnswersResponse(
        int scoreDiff,
        boolean isImproved,
        String growthMessage,
        List<String> newlyIncludedKeywords,
        List<String> stillMissingKeywords,
        String analysis
) {}
