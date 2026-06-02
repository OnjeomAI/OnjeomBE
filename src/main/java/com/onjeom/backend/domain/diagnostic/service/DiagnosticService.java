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
import com.onjeom.backend.domain.learning.enums.CompetencyType;
import com.onjeom.backend.domain.learning.repository.CompetencyScoreRepository;
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
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private final CompetencyScoreRepository competencyScoreRepository;

    private final Map<Long, List<Long>> diagnosticSessions = new ConcurrentHashMap<>();
    private final Map<Long, List<Integer>> diagnosticScores = new ConcurrentHashMap<>();
    private final Map<Long, List<ReadingType>> diagnosticSlots = new ConcurrentHashMap<>();

    private static final List<ReadingType> DEFAULT_SLOT_TYPES = List.of(
            ReadingType.FACTUAL,     ReadingType.FACTUAL,
            ReadingType.INFERENTIAL, ReadingType.INFERENTIAL,
            ReadingType.CRITICAL,    ReadingType.CRITICAL,
            ReadingType.VOCABULARY,  ReadingType.VOCABULARY,
            ReadingType.LOGICAL,     ReadingType.LOGICAL
    );

    private static final Map<CompetencyType, ReadingType> COMPETENCY_TO_READING = Map.of(
            CompetencyType.FACTUAL,     ReadingType.FACTUAL,
            CompetencyType.INFERENTIAL, ReadingType.INFERENTIAL,
            CompetencyType.CRITICAL,    ReadingType.CRITICAL,
            CompetencyType.VOCABULARY,  ReadingType.VOCABULARY,
            CompetencyType.LOGICAL,     ReadingType.LOGICAL
    );

    private List<ReadingType> buildSlotTypes(User user) {
        List<com.onjeom.backend.domain.learning.entity.CompetencyScore> allScores =
                competencyScoreRepository.findByUserOrderByMeasuredAtDesc(user);

        if (allScores.isEmpty()) return DEFAULT_SLOT_TYPES;

        Map<CompetencyType, Integer> scores = allScores.stream()
                .collect(Collectors.toMap(
                        cs -> cs.getCompetencyType(),
                        cs -> cs.getScore().intValue(),
                        (a, b) -> a  // 동일 타입은 최신값 유지
                ));

        Arrays.stream(CompetencyType.values())
                .filter(t -> !scores.containsKey(t))
                .forEach(t -> scores.put(t, 50));

        // 취약순 정렬 후 weakest 3문제, 나머지 2·2·2·1 배분 (합계 10)
        List<CompetencyType> sorted = Arrays.stream(CompetencyType.values())
                .sorted(Comparator.comparingInt(scores::get))
                .toList();

        int[] counts = {3, 2, 2, 2, 1};
        List<ReadingType> slots = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            ReadingType rt = COMPETENCY_TO_READING.get(sorted.get(i));
            for (int j = 0; j < counts[i]; j++) slots.add(rt);
        }

        log.info("재진단 슬롯 배분 (userId={}): {}", user.getId(),
                slots.stream().map(ReadingType::name).toList());
        return slots;
    }

    public DiagnosticProblemResponse getFirstProblem(Long userId) {
        diagnosticSessions.remove(userId);
        diagnosticScores.remove(userId);
        diagnosticSlots.remove(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<ReadingType> slots = buildSlotTypes(user);
        diagnosticSlots.put(userId, slots);

        ReadingType firstType = slots.get(0);
        List<Problem> problems = problemRepository.findByReadingTypeAndDifficulty(firstType, 3);
        if (problems.isEmpty()) problems = problemRepository.findByReadingType(firstType);
        if (problems.isEmpty()) problems = problemRepository.findByDifficulty(3);
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
                problem.getModelAnswer(), List.of(), request.answerText(),
                problem.getReadingType().name()).score();
        scores.add(score);

        if (scores.size() == 10) {
            return completeSession(userId, session, scores);
        }

        return selectNextProblem(userId, session, scores, score);
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
        List<Problem> problems = session.stream()
                .map(id -> problemRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        BigDecimal theta = calculateTheta(problems, scores);
        Map<String, Integer> competencyScores = calculateCompetencyScores(problems, scores);
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
        diagnosticSlots.remove(userId);

        return new DiagnosticNextProblemResponse(true, null, toResultResponse(result, curriculum.getId()));
    }

    private DiagnosticNextProblemResponse selectNextProblem(
            Long userId, List<Long> session, List<Integer> scores, int lastScore) {
        List<ReadingType> slots = diagnosticSlots.getOrDefault(userId, DEFAULT_SLOT_TYPES);
        ReadingType nextType = slots.get(scores.size());

        Problem lastProblem = problemRepository.findById(session.get(session.size() - 1))
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));
        int currentDiff = lastProblem.getDifficulty();
        int nextDiff = lastScore >= 70
                ? Math.min(currentDiff + 1, 5)
                : Math.max(currentDiff - 1, 1);

        List<Problem> candidates = problemRepository.findByReadingTypeAndDifficulty(nextType, nextDiff).stream()
                .filter(p -> !session.contains(p.getId()))
                .toList();

        if (candidates.isEmpty()) {
            candidates = problemRepository.findByReadingType(nextType).stream()
                    .filter(p -> !session.contains(p.getId()))
                    .toList();
        }
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

    private BigDecimal calculateTheta(List<Problem> problems, List<Integer> scores) {
        if (scores.isEmpty()) return BigDecimal.ZERO;

        List<Integer> difficulties = new ArrayList<>();
        List<Integer> responses = new ArrayList<>();
        for (int i = 0; i < Math.min(problems.size(), scores.size()); i++) {
            difficulties.add(problems.get(i).getDifficulty());
            responses.add(scores.get(i) >= 60 ? 1 : 0);
        }

        long correct = responses.stream().filter(r -> r == 1).count();
        if (correct == responses.size()) return BigDecimal.valueOf(2.5).setScale(3, RoundingMode.HALF_UP);
        if (correct == 0) return BigDecimal.valueOf(-2.5).setScale(3, RoundingMode.HALF_UP);

        // 2PL IRT Newton-Raphson MLE (a=1 고정, c=0)
        final double D = 1.702;
        double theta = 0.0;
        for (int iter = 0; iter < 50; iter++) {
            double grad = 0.0, hess = 0.0;
            for (int i = 0; i < difficulties.size(); i++) {
                double b = difficultyToB(difficulties.get(i));
                double p = 1.0 / (1.0 + Math.exp(-D * (theta - b)));
                grad += D * (responses.get(i) - p);
                hess -= D * D * p * (1 - p);
            }
            if (Math.abs(hess) < 1e-10) break;
            double delta = grad / hess;
            theta -= delta;
            theta = Math.max(-3.0, Math.min(3.0, theta));
            if (Math.abs(delta) < 1e-6) break;
        }

        return BigDecimal.valueOf(theta).setScale(3, RoundingMode.HALF_UP);
    }

    private double difficultyToB(int difficulty) {
        return switch (difficulty) {
            case 1 -> -2.0;
            case 2 -> -1.0;
            case 4 -> 1.0;
            case 5 -> 2.0;
            default -> 0.0;
        };
    }

    private Map<String, Integer> calculateCompetencyScores(List<Problem> problems, List<Integer> scores) {
        Map<String, List<Integer>> byType = new HashMap<>();
        for (int i = 0; i < Math.min(problems.size(), scores.size()); i++) {
            String key = readingTypeToKey(problems.get(i).getReadingType());
            if (key == null) continue;  // CREATIVE는 역량 미반영
            byType.computeIfAbsent(key, k -> new ArrayList<>()).add(scores.get(i));
        }

        int totalAvg = (int) scores.stream().mapToInt(Integer::intValue).average().orElse(50.0);

        Map<String, Integer> result = new HashMap<>();
        result.put("factual",     avg(byType.getOrDefault("factual",     List.of(totalAvg))));
        result.put("inferential", avg(byType.getOrDefault("inferential", List.of(totalAvg))));
        result.put("critical",    avg(byType.getOrDefault("critical",    List.of(totalAvg))));
        result.put("vocabulary",  avg(byType.getOrDefault("vocabulary",  List.of(totalAvg))));
        result.put("logical",     avg(byType.getOrDefault("logical",     List.of(totalAvg))));
        return result;
    }

    private String readingTypeToKey(ReadingType type) {
        return switch (type) {
            case FACTUAL     -> "factual";
            case INFERENTIAL -> "inferential";
            case CRITICAL    -> "critical";
            case CREATIVE    -> null;  // 역량 미반영 — 심화 콘텐츠 전용
            case VOCABULARY  -> "vocabulary";
            case LOGICAL     -> "logical";
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
