package com.onjeom.backend.domain.diagnostic.service;

import com.onjeom.backend.domain.ai.service.AiScoringService;
import com.onjeom.backend.domain.curriculum.entity.Curriculum;
import com.onjeom.backend.domain.curriculum.service.CurriculumService;
import com.onjeom.backend.domain.diagnostic.dto.request.DiagnosticAnswerRequest;
import com.onjeom.backend.domain.diagnostic.dto.response.DiagnosticNextProblemResponse;
import com.onjeom.backend.domain.diagnostic.dto.response.DiagnosticProblemResponse;
import com.onjeom.backend.domain.diagnostic.dto.response.DiagnosticResultResponse;
import com.onjeom.backend.domain.diagnostic.entity.DiagnosticResult;
import com.onjeom.backend.domain.diagnostic.repository.DiagnosticResultRepository;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.enums.ReadingType;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.domain.user.repository.UserRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosticService {

    private final ProblemRepository problemRepository;
    private final DiagnosticResultRepository diagnosticResultRepository;
    private final AiScoringService aiScoringService;
    private final CurriculumService curriculumService;
    private final UserRepository userRepository;

    private final Map<Long, List<Long>> diagnosticSessions = new ConcurrentHashMap<>();
    private final Map<Long, List<Integer>> diagnosticScores = new ConcurrentHashMap<>();

    public DiagnosticProblemResponse getFirstProblem(Long userId) {
        diagnosticSessions.remove(userId);
        diagnosticScores.remove(userId);

        List<Problem> problems = problemRepository.findByDifficulty(3);
        if (problems.isEmpty()) throw new CustomException(ErrorCode.NO_PROBLEM_AVAILABLE);

        Problem first = problems.get(new Random().nextInt(problems.size()));

        List<Long> session = new ArrayList<>();
        session.add(first.getId());
        diagnosticSessions.put(userId, session);
        diagnosticScores.put(userId, new ArrayList<>());

        return new DiagnosticProblemResponse(
                first.getId(), 1, first.getPassageText(), first.getQuestionText(), 10);
    }

    public DiagnosticNextProblemResponse submitAnswer(Long userId, DiagnosticAnswerRequest request) {
        List<Long> session = diagnosticSessions.get(userId);
        if (session == null) throw new CustomException(ErrorCode.DIAGNOSTIC_SESSION_NOT_FOUND);

        List<Integer> scores = diagnosticScores.computeIfAbsent(userId, k -> new ArrayList<>());
        if (scores.size() >= 10) throw new CustomException(ErrorCode.DIAGNOSTIC_ALREADY_COMPLETED);

        Problem problem = problemRepository.findById(request.problemId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        int score = aiScoringService.scoreAnswer(
                problem.getPassageText(), problem.getQuestionText(),
                problem.getModelAnswer(), List.of(), request.answerText()).score();
        scores.add(score);

        if (scores.size() == 10) {
            return completeSession(userId, session, scores);
        }

        return selectNextProblem(session, scores, score);
    }

    @Transactional(readOnly = true)
    public DiagnosticResultResponse getLatestResult(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        DiagnosticResult result = diagnosticResultRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));
        Long curriculumId = curriculumService.findCurriculumIdByDiagnostic(result);
        return toResultResponse(result, curriculumId);
    }

    private DiagnosticNextProblemResponse completeSession(
            Long userId, List<Long> session, List<Integer> scores) {
        BigDecimal theta = calculateTheta(scores);
        Map<String, Integer> competencyScores = calculateCompetencyScores(session, scores);
        int level = determineLevel(theta);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        DiagnosticResult result = DiagnosticResult.builder()
                .user(user)
                .theta(theta)
                .factualScore(competencyScores.getOrDefault("factual", 50))
                .inferentialScore(competencyScores.getOrDefault("inferential", 50))
                .criticalScore(competencyScores.getOrDefault("critical", 50))
                .vocabularyScore(competencyScores.getOrDefault("vocabulary", 50))
                .logicalScore(competencyScores.getOrDefault("logical", 50))
                .level(level)
                .build();
        diagnosticResultRepository.save(result);

        Curriculum curriculum = curriculumService.createCurriculum(userId, result);

        diagnosticSessions.remove(userId);
        diagnosticScores.remove(userId);

        return new DiagnosticNextProblemResponse(true, null, toResultResponse(result, curriculum.getId()));
    }

    private DiagnosticNextProblemResponse selectNextProblem(
            List<Long> session, List<Integer> scores, int lastScore) {
        Problem lastProblem = problemRepository.findById(session.get(session.size() - 1))
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));
        int currentDiff = lastProblem.getDifficulty();
        int nextDiff = lastScore >= 70
                ? Math.min(currentDiff + 1, 5)
                : Math.max(currentDiff - 1, 1);

        List<Problem> candidates = problemRepository.findByDifficulty(nextDiff).stream()
                .filter(p -> !session.contains(p.getId()))
                .toList();

        if (candidates.isEmpty()) {
            candidates = problemRepository.findAll().stream()
                    .filter(p -> !session.contains(p.getId()))
                    .toList();
        }
        if (candidates.isEmpty()) throw new CustomException(ErrorCode.NO_PROBLEM_AVAILABLE);

        Problem next = candidates.get(new Random().nextInt(candidates.size()));
        session.add(next.getId());

        int questionNumber = scores.size() + 1;
        DiagnosticProblemResponse nextProblem = new DiagnosticProblemResponse(
                next.getId(), questionNumber, next.getPassageText(), next.getQuestionText(), 10);

        return new DiagnosticNextProblemResponse(false, nextProblem, null);
    }

    private BigDecimal calculateTheta(List<Integer> scores) {
        // TODO: 실제 IRT 3PL 모델로 교체 — STEP 6
        double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(50.0);
        double theta = (avg - 50.0) / 100.0 * 6.0;
        return BigDecimal.valueOf(theta).setScale(3, RoundingMode.HALF_UP);
    }

    private Map<String, Integer> calculateCompetencyScores(List<Long> problemIds, List<Integer> scores) {
        Map<String, List<Integer>> byType = new HashMap<>();
        for (int i = 0; i < problemIds.size(); i++) {
            Problem p = problemRepository.findById(problemIds.get(i)).orElse(null);
            if (p == null) continue;
            String key = readingTypeToKey(p.getReadingType());
            byType.computeIfAbsent(key, k -> new ArrayList<>()).add(scores.get(i));
        }

        int totalAvg = (int) scores.stream().mapToInt(Integer::intValue).average().orElse(50.0);

        Map<String, Integer> result = new HashMap<>();
        result.put("factual",     avg(byType.getOrDefault("factual",     List.of(totalAvg))));
        result.put("inferential", avg(byType.getOrDefault("inferential", List.of(totalAvg))));
        result.put("critical",    avg(byType.getOrDefault("critical",    List.of(totalAvg))));
        result.put("vocabulary",  totalAvg);
        result.put("logical",     totalAvg);
        return result;
    }

    private String readingTypeToKey(ReadingType type) {
        return switch (type) {
            case FACTUAL      -> "factual";
            case INFERENTIAL  -> "inferential";
            case CRITICAL, CREATIVE -> "critical";
        };
    }

    private int avg(List<Integer> list) {
        return list.isEmpty() ? 50 : (int) list.stream().mapToInt(Integer::intValue).average().orElse(50.0);
    }

    private int determineLevel(BigDecimal theta) {
        double t = theta.doubleValue();
        if (t < 0.0) return 1;
        if (t < 0.5) return 2;
        return 3;
    }

    private DiagnosticResultResponse toResultResponse(DiagnosticResult result, Long curriculumId) {
        return new DiagnosticResultResponse(
                result.getId(),
                result.getTheta(),
                result.getFactualScore(),
                result.getInferentialScore(),
                result.getCriticalScore(),
                result.getVocabularyScore(),
                result.getLogicalScore(),
                result.getLevel(),
                curriculumId
        );
    }
}
