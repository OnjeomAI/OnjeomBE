package com.onjeom.backend.domain.dashboard.service;

import com.onjeom.backend.domain.dashboard.dto.response.*;
import com.onjeom.backend.domain.learning.entity.CompetencyScore;
import com.onjeom.backend.domain.learning.enums.CompetencyType;
import com.onjeom.backend.domain.learning.repository.CompetencyScoreRepository;
import com.onjeom.backend.domain.learning.repository.ReviewScheduleRepository;
import com.onjeom.backend.domain.learning.dto.response.ReviewScheduleResponse;
import com.onjeom.backend.domain.response.entity.Response;
import com.onjeom.backend.domain.response.repository.ResponseRepository;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.domain.user.repository.UserRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final ResponseRepository responseRepository;
    private final CompetencyScoreRepository competencyScoreRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;

    public RadarChartResponse getRadarChart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<RadarChartResponse.CompetencyDetail> competencies = Arrays.stream(CompetencyType.values())
                .map(type -> competencyScoreRepository
                        .findTopByUserAndCompetencyTypeOrderByMeasuredAtDesc(user, type)
                        .map(cs -> new RadarChartResponse.CompetencyDetail(
                                cs.getCompetencyType(),
                                cs.getScore(),
                                cs.getLevel(),
                                cs.getDelta()
                        ))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        return new RadarChartResponse(competencies);
    }

    public LearningStatsResponse getLearningStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Response> allResponses = responseRepository.findByUserOrderByCreatedAtDesc(user);

        int totalResponses = allResponses.size();
        double averageScore = allResponses.stream()
                .mapToInt(Response::getFinalScore)
                .average()
                .orElse(0.0);

        int streakDays = calculateStreakDays(allResponses);

        Map<LocalDate, List<Response>> byDate = allResponses.stream()
                .filter(r -> r.getCreatedAt() != null)
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().toLocalDate()));

        List<LearningStatsResponse.StatPoint> recentStats = LocalDate.now()
                .minusDays(6)
                .datesUntil(LocalDate.now().plusDays(1))
                .map(date -> {
                    List<Response> dayResponses = byDate.getOrDefault(date, List.of());
                    double avg = dayResponses.stream()
                            .mapToInt(Response::getFinalScore)
                            .average()
                            .orElse(0.0);
                    return new LearningStatsResponse.StatPoint(date, dayResponses.size(), avg);
                })
                .toList();

        return new LearningStatsResponse(totalResponses, Math.round(averageScore * 10.0) / 10.0, streakDays, recentStats);
    }

    public TodayGoalResponse getTodayGoal(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<Response> todayResponses = responseRepository.findByUserAndCreatedAtAfter(user, startOfDay);
        int completedToday = todayResponses.size();
        int dailyGoal = user.getDailyGoal();
        boolean goalAchieved = completedToday >= dailyGoal;

        List<ReviewScheduleResponse> dueReviews = reviewScheduleRepository
                .findByUserAndNextReviewAtBefore(user, LocalDateTime.now())
                .stream()
                .map(schedule -> new ReviewScheduleResponse(
                        schedule.getId(),
                        schedule.getProblem().getId(),
                        schedule.getProblem().getQuestionText(),
                        schedule.getProblem().getReadingType(),
                        schedule.getReviewCount(),
                        schedule.getNextReviewAt(),
                        schedule.getLastReviewedAt()
                ))
                .toList();

        return new TodayGoalResponse(dailyGoal, completedToday, goalAchieved, dueReviews);
    }

    public RecentResponseResponse getRecentResponses(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Response> responsePage = responseRepository.findByUserOrderByCreatedAtDesc(user, pageRequest);

        List<RecentResponseResponse.ResponseSummary> summaries = responsePage.getContent().stream()
                .map(r -> new RecentResponseResponse.ResponseSummary(
                        r.getId(),
                        r.getProblem().getId(),
                        r.getProblem().getQuestionText(),
                        r.getProblem().getReadingType(),
                        r.getFinalScore(),
                        r.getCreatedAt()
                ))
                .toList();

        return new RecentResponseResponse(
                summaries,
                (int) responsePage.getTotalElements(),
                responsePage.getNumber(),
                responsePage.getTotalPages()
        );
    }

    public WeakPointReportResponse getWeakPointReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<WeakPointReportResponse.WeakCompetency> weakCompetencies = Arrays.stream(CompetencyType.values())
                .map(type -> competencyScoreRepository
                        .findTopByUserAndCompetencyTypeOrderByMeasuredAtDesc(user, type)
                        .filter(cs -> cs.getScore().doubleValue() < 60.0)
                        .map(cs -> new WeakPointReportResponse.WeakCompetency(
                                cs.getCompetencyType(),
                                cs.getScore(),
                                cs.getLevel()
                        ))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        int reviewDueCount = reviewScheduleRepository
                .findByUserAndNextReviewAtBefore(user, LocalDateTime.now())
                .size();

        return new WeakPointReportResponse(weakCompetencies, reviewDueCount);
    }

    public AdminStatsResponse getAdminStats() {
        long totalUsers = userRepository.count();
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(
                LocalDate.now().withDayOfMonth(1).atStartOfDay());
        long totalResponses = responseRepository.count();
        long activeUsersThisMonth = responseRepository.countDistinctUsersByCreatedAtAfter(
                LocalDate.now().withDayOfMonth(1).atStartOfDay());
        Double avgScore = responseRepository.findAverageFinalScore();
        double overallAverageScore = avgScore != null
                ? Math.round(avgScore * 10.0) / 10.0
                : 0.0;

        List<CompetencyScore> allScores = competencyScoreRepository.findAll();

        Map<Long, Map<CompetencyType, CompetencyScore>> latestPerUserAndType = new HashMap<>();
        for (CompetencyScore cs : allScores) {
            Long uid = cs.getUser().getId();
            latestPerUserAndType
                    .computeIfAbsent(uid, k -> new HashMap<>())
                    .merge(cs.getCompetencyType(), cs,
                            (a, b) -> a.getMeasuredAt().isAfter(b.getMeasuredAt()) ? a : b);
        }

        List<AdminStatsResponse.CompetencyWeakStat> competencyStats = Arrays.stream(CompetencyType.values())
                .map(type -> {
                    List<Double> scores = latestPerUserAndType.values().stream()
                            .map(m -> m.get(type))
                            .filter(Objects::nonNull)
                            .map(cs -> cs.getScore().doubleValue())
                            .toList();
                    double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    return new AdminStatsResponse.CompetencyWeakStat(
                            type,
                            Math.round(avg * 10.0) / 10.0,
                            scores.size()
                    );
                })
                .toList();

        return new AdminStatsResponse(
                totalUsers,
                newUsersThisMonth,
                totalResponses,
                activeUsersThisMonth,
                overallAverageScore,
                competencyStats
        );
    }

    private int calculateStreakDays(List<Response> responses) {
        if (responses.isEmpty()) return 0;

        Set<LocalDate> activeDates = responses.stream()
                .filter(r -> r.getCreatedAt() != null)
                .map(r -> r.getCreatedAt().toLocalDate())
                .collect(Collectors.toSet());

        int streak = 0;
        LocalDate check = LocalDate.now();
        while (activeDates.contains(check)) {
            streak++;
            check = check.minusDays(1);
        }
        return streak;
    }
}
