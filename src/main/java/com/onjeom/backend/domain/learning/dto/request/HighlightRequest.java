package com.onjeom.backend.domain.learning.dto.request;

import com.onjeom.backend.domain.learning.enums.HighlightColor;
import jakarta.validation.constraints.NotNull;

public record HighlightRequest(
        @NotNull Long problemId,
        @NotNull Integer startOffset,
        @NotNull Integer endOffset,
        @NotNull HighlightColor color
) {
}
