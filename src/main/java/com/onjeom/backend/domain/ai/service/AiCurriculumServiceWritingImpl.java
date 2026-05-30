package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.AvailableProblemDto;
import com.onjeom.backend.domain.ai.dto.CurriculumPlanAiRequest;
import com.onjeom.backend.domain.ai.dto.CurriculumPlanAiResponse;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.server.impl", havingValue = "writing", matchIfMissing = true)
public class AiCurriculumServiceWritingImpl implements AiCurriculumService {

    private final RestClient aiRestClient;
    private final ProblemRepository problemRepository;

    @Override
    public Map<Integer, List<Long>> generateCurriculumPlan(
            BigDecimal theta, int dailyGoal, Map<String, Integer> competencyScores) {

        List<AvailableProblemDto> availableProblems = problemRepository.findAll().stream()
                .map(p -> new AvailableProblemDto(p.getId(), p.getDifficulty(), p.getReadingType().name()))
                .toList();

        CurriculumPlanAiRequest request = new CurriculumPlanAiRequest(
                theta.doubleValue(),
                dailyGoal,
                competencyScores,
                availableProblems
        );

        try {
            CurriculumPlanAiResponse response = aiRestClient.post()
                    .uri("/api/writing/curriculum-plan")
                    .body(request)
                    .retrieve()
                    .body(CurriculumPlanAiResponse.class);

            if (response == null || response.plan() == null) {
                log.warn("AI 커리큘럼 API 응답이 null입니다. 빈 플랜 반환");
                return Collections.emptyMap();
            }

            log.info("AI 커리큘럼 플랜 생성 완료 - stages={}", response.plan().keySet());
            return response.plan();

        } catch (RestClientException e) {
            log.error("AI 커리큘럼 API 호출 실패: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
