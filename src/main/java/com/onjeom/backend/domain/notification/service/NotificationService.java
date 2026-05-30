package com.onjeom.backend.domain.notification.service;

import com.onjeom.backend.domain.curriculum.entity.Curriculum;
import com.onjeom.backend.domain.curriculum.enums.CurriculumItemStatus;
import com.onjeom.backend.domain.curriculum.enums.CurriculumStatus;
import com.onjeom.backend.domain.curriculum.repository.CurriculumItemRepository;
import com.onjeom.backend.domain.curriculum.repository.CurriculumRepository;
import com.onjeom.backend.domain.learning.entity.CompetencyScore;
import com.onjeom.backend.domain.learning.enums.CompetencyType;
import com.onjeom.backend.domain.learning.repository.CompetencyScoreRepository;
import com.onjeom.backend.domain.learning.repository.ReviewScheduleRepository;
import com.onjeom.backend.domain.notification.dto.response.NotificationResponse;
import com.onjeom.backend.domain.response.repository.ResponseRepository;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.domain.user.repository.UserRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final UserRepository userRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;
    private final ResponseRepository responseRepository;
    private final CompetencyScoreRepository competencyScoreRepository;
    private final CurriculumRepository curriculumRepository;
    private final CurriculumItemRepository curriculumItemRepository;

    public NotificationResponse getNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<NotificationResponse.NotificationItem> items = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        addReviewNotifications(user, now, items);
        addGoalAchievedNotification(user, now, items);
        addWeakPointNotification(user, now, items);
        addCurriculumProgressNotification(user, now, items);
        addNewContentNotification(user, now, items);

        return new NotificationResponse(items);
    }

    private void addReviewNotifications(User user, LocalDateTime now,
                                        List<NotificationResponse.NotificationItem> items) {
        int dueCount = reviewScheduleRepository.findByUserAndNextReviewAtBefore(user, now).size();
        if (dueCount > 0) {
            items.add(new NotificationResponse.NotificationItem(
                    "REVIEW",
                    "복습 알림",
                    "오늘 복습할 문제가 " + dueCount + "개 있습니다.",
                    now
            ));
        }
    }

    private void addGoalAchievedNotification(User user, LocalDateTime now,
                                              List<NotificationResponse.NotificationItem> items) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        int completedToday = responseRepository.findByUserAndCreatedAtAfter(user, startOfDay).size();
        if (completedToday >= user.getDailyGoal()) {
            items.add(new NotificationResponse.NotificationItem(
                    "GOAL_ACHIEVED",
                    "목표 달성",
                    "오늘의 학습 목표 " + user.getDailyGoal() + "문제를 달성했습니다! 수고하셨습니다.",
                    now
            ));
        }
    }

    private void addWeakPointNotification(User user, LocalDateTime now,
                                           List<NotificationResponse.NotificationItem> items) {
        long weakCount = Arrays.stream(CompetencyType.values())
                .map(type -> competencyScoreRepository
                        .findTopByUserAndCompetencyTypeOrderByMeasuredAtDesc(user, type)
                        .filter(cs -> cs.getScore().doubleValue() < 60.0)
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .count();

        if (weakCount > 0) {
            items.add(new NotificationResponse.NotificationItem(
                    "WEAK_POINT",
                    "약점 진단",
                    "취약한 역량이 " + weakCount + "개 감지되었습니다. 집중 학습을 권장합니다.",
                    now
            ));
        }
    }

    private void addCurriculumProgressNotification(User user, LocalDateTime now,
                                                    List<NotificationResponse.NotificationItem> items) {
        Optional<Curriculum> curriculum = curriculumRepository
                .findTopByUserAndStatusOrderByCreatedAtDesc(user, CurriculumStatus.ACTIVE);
        if (curriculum.isEmpty()) return;

        Curriculum c = curriculum.get();
        int total = curriculumItemRepository.findByCurriculumOrderByStageAscOrderIndexAsc(c).size();
        int completed = curriculumItemRepository.countByCurriculumAndStatus(c, CurriculumItemStatus.COMPLETED);

        if (total > 0 && completed > 0) {
            int percent = (int) Math.round((double) completed / total * 100);
            items.add(new NotificationResponse.NotificationItem(
                    "CURRICULUM_PROGRESS",
                    "커리큘럼 진행",
                    "커리큘럼을 " + percent + "% 완료했습니다. (" + completed + "/" + total + ")",
                    now
            ));
        }
    }

    private void addNewContentNotification(User user, LocalDateTime now,
                                            List<NotificationResponse.NotificationItem> items) {
        Optional<Curriculum> curriculum = curriculumRepository
                .findTopByUserAndStatusOrderByCreatedAtDesc(user, CurriculumStatus.ACTIVE);
        if (curriculum.isEmpty()) return;

        int pendingCount = curriculumItemRepository
                .countByCurriculumAndStatus(curriculum.get(), CurriculumItemStatus.PENDING);

        if (pendingCount > 0) {
            items.add(new NotificationResponse.NotificationItem(
                    "NEW_CONTENT",
                    "신규 학습",
                    "학습할 문제가 " + pendingCount + "개 준비되어 있습니다.",
                    now
            ));
        }
    }
}
