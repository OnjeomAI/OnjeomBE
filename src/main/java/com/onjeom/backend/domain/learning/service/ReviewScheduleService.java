package com.onjeom.backend.domain.learning.service;

import com.onjeom.backend.domain.learning.dto.response.ReviewScheduleResponse;
import com.onjeom.backend.domain.learning.entity.ReviewSchedule;
import com.onjeom.backend.domain.learning.repository.ReviewScheduleRepository;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.domain.user.repository.UserRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewScheduleService {

    private final ReviewScheduleRepository reviewScheduleRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;

    public List<ReviewScheduleResponse> getTodayReviews(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return reviewScheduleRepository.findByUserAndNextReviewAtBefore(user, LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReviewScheduleResponse> getAllSchedules(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return reviewScheduleRepository.findByUserOrderByNextReviewAtAsc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void createOrUpdateSchedule(Long userId, Long problemId, boolean passed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        Optional<ReviewSchedule> existing = reviewScheduleRepository.findByUserAndProblem(user, problem);

        if (existing.isPresent()) {
            if (passed) {
                existing.get().completeReview();
            }
        } else {
            ReviewSchedule schedule = ReviewSchedule.builder()
                    .user(user)
                    .problem(problem)
                    .reviewCount(0)
                    .nextReviewAt(LocalDateTime.now().plusDays(1))
                    .lastReviewedAt(null)
                    .build();
            reviewScheduleRepository.save(schedule);
        }
    }

    private ReviewScheduleResponse toResponse(ReviewSchedule schedule) {
        return new ReviewScheduleResponse(
                schedule.getId(),
                schedule.getProblem().getId(),
                schedule.getProblem().getQuestionText(),
                schedule.getProblem().getReadingType(),
                schedule.getReviewCount(),
                schedule.getNextReviewAt(),
                schedule.getLastReviewedAt()
        );
    }
}
