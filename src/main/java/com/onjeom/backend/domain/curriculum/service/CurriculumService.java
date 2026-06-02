package com.onjeom.backend.domain.curriculum.service;

import com.onjeom.backend.domain.curriculum.dto.response.CurriculumItemResponse;
import com.onjeom.backend.domain.curriculum.dto.response.CurriculumProgressResponse;
import com.onjeom.backend.domain.curriculum.dto.response.CurriculumResponse;
import com.onjeom.backend.domain.curriculum.entity.Curriculum;
import com.onjeom.backend.domain.curriculum.entity.CurriculumItem;
import com.onjeom.backend.domain.curriculum.enums.CurriculumItemStatus;
import com.onjeom.backend.domain.curriculum.enums.CurriculumStatus;
import com.onjeom.backend.domain.curriculum.repository.CurriculumItemRepository;
import com.onjeom.backend.domain.curriculum.repository.CurriculumRepository;
import com.onjeom.backend.domain.ai.service.AiCurriculumService;
import com.onjeom.backend.domain.diagnostic.entity.DiagnosticResult;
import com.onjeom.backend.domain.diagnostic.repository.DiagnosticResultRepository;
import com.onjeom.backend.domain.learning.enums.CompetencyType;
import com.onjeom.backend.domain.learning.repository.CompetencyScoreRepository;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.domain.user.repository.UserRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final CurriculumItemRepository curriculumItemRepository;
    private final ProblemRepository problemRepository;
    private final AiCurriculumService aiCurriculumService;
    private final UserRepository userRepository;
    private final CompetencyScoreRepository competencyScoreRepository;
    private final DiagnosticResultRepository diagnosticResultRepository;

    public Curriculum createCurriculum(Long userId, DiagnosticResult diagnostic) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Map<String, Integer> competencyScores = Map.of(
                "factual", diagnostic.getFactualScore(),
                "inferential", diagnostic.getInferentialScore(),
                "critical", diagnostic.getCriticalScore(),
                "vocabulary", diagnostic.getVocabularyScore(),
                "logical", diagnostic.getLogicalScore()
        );

        Map<Integer, List<Long>> plan = aiCurriculumService.generateCurriculumPlan(
                diagnostic.getTheta(), user.getDailyGoal(), competencyScores);

        return buildAndPersistCurriculum(user, diagnostic, plan);
    }

    @Transactional(readOnly = true)
    public CurriculumResponse getMyCurriculum(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Curriculum curriculum = curriculumRepository
                .findTopByUserAndStatusOrderByCreatedAtDesc(user, CurriculumStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.CURRICULUM_NOT_FOUND));

        List<CurriculumItem> allItems = curriculumItemRepository
                .findByCurriculumOrderByStageAscOrderIndexAsc(curriculum);
        int totalItems = allItems.size();
        int completedItems = curriculumItemRepository
                .countByCurriculumAndStatus(curriculum, CurriculumItemStatus.COMPLETED);

        List<CurriculumItemResponse> todayItems = allItems.stream()
                .filter(item -> item.getStage().equals(curriculum.getCurrentStage())
                        && item.getStatus() == CurriculumItemStatus.PENDING)
                .sorted(Comparator.comparing(CurriculumItem::getOrderIndex))
                .limit(user.getDailyGoal())
                .map(this::toItemResponse)
                .toList();

        return new CurriculumResponse(
                curriculum.getId(),
                curriculum.getStatus(),
                curriculum.getCurrentStage(),
                totalItems,
                completedItems,
                todayItems
        );
    }

    @Transactional(readOnly = true)
    public CurriculumProgressResponse getProgress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Curriculum curriculum = curriculumRepository
                .findTopByUserAndStatusOrderByCreatedAtDesc(user, CurriculumStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.CURRICULUM_NOT_FOUND));

        int totalItems = curriculumItemRepository
                .findByCurriculumOrderByStageAscOrderIndexAsc(curriculum).size();
        int completedItems = curriculumItemRepository
                .countByCurriculumAndStatus(curriculum, CurriculumItemStatus.COMPLETED);
        int skippedItems = curriculumItemRepository
                .countByCurriculumAndStatus(curriculum, CurriculumItemStatus.SKIPPED);

        double progressPercent = totalItems > 0
                ? Math.round((double) completedItems / totalItems * 1000.0) / 10.0
                : 0.0;

        return new CurriculumProgressResponse(
                curriculum.getId(),
                curriculum.getCurrentStage(),
                totalItems,
                completedItems,
                skippedItems,
                progressPercent
        );
    }

    @Transactional(readOnly = true)
    public Long findCurriculumIdByDiagnostic(DiagnosticResult diagnostic) {
        return curriculumRepository.findByDiagnostic(diagnostic)
                .map(Curriculum::getId)
                .orElse(null);
    }

    public CurriculumItemResponse startItem(Long userId, Long itemId) {
        CurriculumItem item = curriculumItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CURRICULUM_NOT_FOUND));
        if (!item.getCurriculum().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        item.start();
        return toItemResponse(item);
    }

    public CurriculumItemResponse skipItem(Long userId, Long itemId) {
        CurriculumItem item = curriculumItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CURRICULUM_NOT_FOUND));
        if (!item.getCurriculum().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        item.skip();
        return toItemResponse(item);
    }

    public CurriculumItemResponse completeItem(Long userId, Long itemId) {
        CurriculumItem item = curriculumItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CURRICULUM_NOT_FOUND));
        if (!item.getCurriculum().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        item.complete();

        int stage = item.getStage();
        boolean stageComplete = curriculumItemRepository
                .findByCurriculumOrderByStageAscOrderIndexAsc(item.getCurriculum()).stream()
                .filter(i -> i.getStage().equals(stage))
                .allMatch(i -> i.getStatus() == CurriculumItemStatus.COMPLETED
                        || i.getStatus() == CurriculumItemStatus.SKIPPED);

        if (stageComplete) {
            log.info("스테이지 {} 완료 → 역량 기반 커리큘럼 재생성 (userId={})", stage, userId);
            refreshCurriculumWithCurrentScores(userId);
        }

        return toItemResponse(item);
    }

    private void refreshCurriculumWithCurrentScores(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        DiagnosticResult diagnostic = diagnosticResultRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElse(null);
        if (diagnostic == null) return;

        Map<CompetencyType, Integer> latestScores = competencyScoreRepository
                .findByUserOrderByMeasuredAtDesc(user).stream()
                .collect(Collectors.toMap(
                        cs -> cs.getCompetencyType(),
                        cs -> cs.getScore().intValue(),
                        (a, b) -> a  // 동일 타입은 최신값 유지
                ));

        Map<String, Integer> competencyScores = new HashMap<>();
        for (CompetencyType type : CompetencyType.values()) {
            int score = latestScores.getOrDefault(type, fallbackDiagnosticScore(diagnostic, type));
            competencyScores.put(type.name().toLowerCase(), score);
        }

        Map<Integer, List<Long>> plan = aiCurriculumService.generateCurriculumPlan(
                diagnostic.getTheta(), user.getDailyGoal(), competencyScores);

        buildAndPersistCurriculum(user, diagnostic, plan);
    }

    private Curriculum buildAndPersistCurriculum(User user, DiagnosticResult diagnostic,
                                                  Map<Integer, List<Long>> plan) {
        curriculumRepository.findTopByUserAndStatusOrderByCreatedAtDesc(user, CurriculumStatus.ACTIVE)
                .ifPresent(c -> {
                    c.pause();
                    curriculumRepository.save(c);
                });

        int startStage = plan.isEmpty() ? diagnostic.getLevel()
                : plan.keySet().stream().min(Integer::compareTo).orElse(diagnostic.getLevel());

        Curriculum curriculum = Curriculum.builder()
                .user(user)
                .diagnostic(diagnostic)
                .status(CurriculumStatus.ACTIVE)
                .currentStage(startStage)
                .build();
        curriculumRepository.save(curriculum);

        List<Long> allProblemIds = plan.values().stream().flatMap(List::stream).toList();
        Map<Long, Problem> problemMap = problemRepository.findAllById(allProblemIds).stream()
                .collect(Collectors.toMap(Problem::getId, p -> p));

        for (Map.Entry<Integer, List<Long>> entry : plan.entrySet()) {
            int stage = entry.getKey();
            List<Long> problemIds = entry.getValue();
            for (int i = 0; i < problemIds.size(); i++) {
                Problem problem = problemMap.get(problemIds.get(i));
                if (problem == null) continue;
                curriculumItemRepository.save(CurriculumItem.builder()
                        .curriculum(curriculum)
                        .problem(problem)
                        .stage(stage)
                        .orderIndex(i + 1)
                        .status(CurriculumItemStatus.PENDING)
                        .scheduledAt(null)
                        .completedAt(null)
                        .build());
            }
        }

        return curriculum;
    }

    private int fallbackDiagnosticScore(DiagnosticResult diagnostic, CompetencyType type) {
        return switch (type) {
            case FACTUAL     -> diagnostic.getFactualScore();
            case INFERENTIAL -> diagnostic.getInferentialScore();
            case CRITICAL    -> diagnostic.getCriticalScore();
            case VOCABULARY  -> diagnostic.getVocabularyScore();
            case LOGICAL     -> diagnostic.getLogicalScore();
        };
    }

    private CurriculumItemResponse toItemResponse(CurriculumItem item) {
        return new CurriculumItemResponse(
                item.getId(),
                item.getProblem().getId(),
                item.getProblem().getQuestionText(),
                item.getProblem().getReadingType(),
                item.getProblem().getDifficulty(),
                item.getStage(),
                item.getOrderIndex(),
                item.getStatus(),
                item.getScheduledAt()
        );
    }
}
