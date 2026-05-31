package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.IrtEstimateRequest;
import com.onjeom.backend.domain.ai.dto.IrtEstimateResponse;

public interface AiIrtService {
    IrtEstimateResponse estimateAbility(IrtEstimateRequest request);
}
