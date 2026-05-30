package com.onjeom.backend.domain.learning.dto.response;

import com.onjeom.backend.domain.learning.enums.HighlightColor;

import java.time.LocalDateTime;

public record HighlightResponse(
        Long id,
        Long problemId,
        Integer startOffset,
        Integer endOffset,
        HighlightColor color,
        LocalDateTime createdAt
) {
}
