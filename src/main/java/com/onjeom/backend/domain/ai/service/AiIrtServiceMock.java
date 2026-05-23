package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.IrtEstimateRequest;
import com.onjeom.backend.domain.ai.dto.IrtEstimateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.scoring.enabled", havingValue = "false", matchIfMissing = true)
public class AiIrtServiceMock implements AiIrtService {

    @Override
    public IrtEstimateResponse estimateAbility(IrtEstimateRequest request) {
        log.info("[MOCK] estimateAbility 호출 - {}개 응답", request.responses().size());

        double avg = request.responses().stream()
                .mapToInt(r -> r.score())
                .average()
                .orElse(50.0);

        double theta = (avg - 50.0) / 100.0 * 6.0;
        BigDecimal thetaBd = BigDecimal.valueOf(theta).setScale(4, RoundingMode.HALF_UP);

        String level;
        int nextDiff;
        if (theta < -1.5)      { level = "하";  nextDiff = 1; }
        else if (theta < -0.5) { level = "중하"; nextDiff = 2; }
        else if (theta < 0.5)  { level = "중";  nextDiff = 3; }
        else if (theta < 1.5)  { level = "중상"; nextDiff = 4; }
        else                   { level = "상";  nextDiff = 5; }

        return new IrtEstimateResponse(thetaBd, BigDecimal.valueOf(0.8), level, nextDiff);
    }
}
