package com.onjeom.backend.domain.ai.dto;

import java.util.List;

public record IrtEstimateRequest(
        List<IrtResponseItemDto> responses
) {}
