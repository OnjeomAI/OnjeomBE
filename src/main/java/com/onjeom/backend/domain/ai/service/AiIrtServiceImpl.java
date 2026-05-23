package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.IrtEstimateRequest;
import com.onjeom.backend.domain.ai.dto.IrtEstimateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.scoring.enabled", havingValue = "true")
@RequiredArgsConstructor
public class AiIrtServiceImpl implements AiIrtService {

    private final RestClient aiRestClient;

    @Override
    public IrtEstimateResponse estimateAbility(IrtEstimateRequest request) {
        try {
            IrtEstimateResponse response = aiRestClient.post()
                    .uri("/api/writing/irt/estimate")
                    .body(request)
                    .retrieve()
                    .body(IrtEstimateResponse.class);

            if (response == null) {
                log.warn("AI IRT 응답이 null입니다. 기본값 반환");
                return fallback();
            }

            log.info("IRT 능력 추정 완료 - theta={}, abilityLevel={}", response.theta(), response.abilityLevel());
            return response;

        } catch (RestClientException e) {
            log.error("AI IRT 호출 실패: {}", e.getMessage());
            return fallback();
        }
    }

    private IrtEstimateResponse fallback() {
        return new IrtEstimateResponse(BigDecimal.ZERO, BigDecimal.ONE, "중", 3);
    }
}
