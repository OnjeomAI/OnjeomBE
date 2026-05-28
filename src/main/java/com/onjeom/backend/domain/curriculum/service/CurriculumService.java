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
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.domain.user.repository.UserRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final CurriculumItemRepository curriculumItemRepository;
    private final ProblemRepository problemRepository;
    private final AiCurriculumService aiCurriculumService;
    private final UserRepository userRepository;

    public Curriculum createCurriculum(Long userId, DiagnosticResult diagnostic) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        curriculumRepository.findTopByUserAndStatusOrderByCreatedAtDesc(user, CurriculumStatus.ACTIVE)
                .ifPresent(c -> {
                    c.pause();
                    curriculumRepository.save(c);
                });

        Map<String, Integer> competencyScores = Map.of(
                "factual", diagnostic.getFactualScore(),
                "inferential", diagnostic.getInferentialScore(),
                "critical", diagnostic.getCriticalScore(),
                "vocabulary", diagnostic.getVocabularyScore(),
                "logical", diagnostic.getLogicalScore()
        );

        Map<Integer, List<Long>> plan = aiCurriculumService.generateCurriculumPlan(
                diagnostic.getTheta(), user.getDailyGoal(), competencyScores);

        Curriculum curriculum = Curriculum.builder()
                .user(user)
                .diagnostic(diagnostic)
                .status(CurriculumStatus.ACTIVE)
                .currentStage(diagnostic.getLevel())
                .build();
        curriculumRepository.save(curriculum);

        for (Map.Entry<Integer, List<Long>> entry : plan.entrySet()) {
            int stage = entry.getKey();
            List<Long> problemIds = entry.getValue();
            for (int i = 0; i < problemIds.size(); i++) {
                Problem problem = problemRepository.findById(problemIds.get(i)).orElse(null);
                if (problem == null) continue;
                CurriculumItem item = CurriculumItem.builder()
                        .curriculum(curriculum)
                        .problem(problem)
                        .stage(stage)
                        .orderIndex(i + 1)
                        .status(CurriculumItemStatus.PENDING)
                        .scheduledAt(null)
                        .completedAt(null)
                        .build();
                curriculumItemRepository.save(item);
            }
        }

        return curriculum;
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
