package com.onjeom.backend.domain.learning.service;

import com.onjeom.backend.domain.learning.dto.response.CompetencyScoreResponse;
import com.onjeom.backend.domain.learning.dto.response.KnowledgeTracingResponse;
import com.onjeom.backend.domain.learning.entity.CompetencyScore;
import com.onjeom.backend.domain.learning.entity.KnowledgeTracing;
import com.onjeom.backend.domain.learning.enums.CompetencyLevel;
import com.onjeom.backend.domain.learning.enums.CompetencyType;
import com.onjeom.backend.domain.learning.repository.CompetencyScoreRepository;
import com.onjeom.backend.domain.learning.repository.KnowledgeTracingRepository;
import com.onjeom.backend.domain.problem.enums.ReadingType;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.domain.user.repository.UserRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CompetencyScoreService {

    private final KnowledgeTracingRepository knowledgeTracingRepository;
    private final CompetencyScoreRepository competencyScoreRepository;
    private final UserRepository userRepository;

    public List<CompetencyScoreResponse> updateAfterResponse(Long userId, ReadingType readingType, int finalScore) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        CompetencyType competencyType = readingTypeToCompetencyType(readingType);

        KnowledgeTracing kt = knowledgeTracingRepository
                .findByUserAndCompetencyType(user, competencyType)
                .orElseGet(() -> {
                    KnowledgeTracing newKt = KnowledgeTracing.builder()
                            .user(user)
                            .competencyType(competencyType)
                            .pKnow(new BigDecimal("0.3000"))
                            .pLearn(new BigDecimal("0.1000"))
                            .pSlip(new BigDecimal("0.1000"))
                            .pGuess(new BigDecimal("0.2000"))
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return knowledgeTracingRepository.save(newKt);
                });

        boolean isCorrect = finalScore >= 60;
        kt.update(isCorrect);
        knowledgeTracingRepository.save(kt);

        BigDecimal prevScore = competencyScoreRepository
                .findTopByUserAndCompetencyTypeOrderByMeasuredAtDesc(user, competencyType)
                .map(CompetencyScore::getScore)
                .orElse(BigDecimal.ZERO);

        BigDecimal newScore = kt.getPKnow()
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal delta = newScore.subtract(prevScore).setScale(2, RoundingMode.HALF_UP);
        CompetencyLevel level = CompetencyLevel.from(newScore.doubleValue());

        CompetencyScore competencyScore = CompetencyScore.builder()
                .user(user)
                .competencyType(competencyType)
                .score(newScore)
                .level(level)
                .delta(delta)
                .measuredAt(LocalDateTime.now())
                .build();
        competencyScoreRepository.save(competencyScore);

        return latestScores(user);
    }

    @Transactional(readOnly = true)
    public List<CompetencyScoreResponse> getMyCompetencyScores(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return latestScores(user);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeTracingResponse> getMyKnowledgeTracing(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return knowledgeTracingRepository.findByUser(user).stream()
                .map(kt -> new KnowledgeTracingResponse(
                        kt.getCompetencyType(),
                        kt.getPKnow().doubleValue(),
                        kt.getUpdatedAt()))
                .toList();
    }

    private List<CompetencyScoreResponse> latestScores(User user) {
        return Arrays.stream(CompetencyType.values())
                .map(type -> competencyScoreRepository
                        .findTopByUserAndCompetencyTypeOrderByMeasuredAtDesc(user, type)
                        .map(this::toCompetencyResponse)
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private CompetencyType readingTypeToCompetencyType(ReadingType readingType) {
        return switch (readingType) {
            case FACTUAL     -> CompetencyType.FACTUAL;
            case INFERENTIAL -> CompetencyType.INFERENTIAL;
            case CRITICAL    -> CompetencyType.CRITICAL;
            case CREATIVE    -> CompetencyType.LOGICAL;
        };
    }

    private CompetencyScoreResponse toCompetencyResponse(CompetencyScore cs) {
        return new CompetencyScoreResponse(
                cs.getCompetencyType(),
                cs.getScore(),
                cs.getLevel(),
                cs.getDelta(),
                cs.getMeasuredAt()
        );
    }
}
