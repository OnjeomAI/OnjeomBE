package com.onjeom.backend.domain.user.service;

import com.onjeom.backend.domain.curriculum.service.CurriculumService;
import com.onjeom.backend.domain.diagnostic.repository.DiagnosticResultRepository;
import com.onjeom.backend.domain.user.dto.request.UpdateProfileRequest;
import com.onjeom.backend.domain.user.dto.response.UserProfileResponse;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.domain.user.repository.UserRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final DiagnosticResultRepository diagnosticResultRepository;
    private final CurriculumService curriculumService;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return toResponse(user);
    }

    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        boolean goalChanged = !Objects.equals(user.getDailyGoal(), request.dailyGoal());
        user.updateProfile(request.nickname(), request.alarmEnabled(), request.dailyGoal(), request.fontSize());

        if (goalChanged) {
            diagnosticResultRepository.findTopByUserOrderByCreatedAtDesc(user).ifPresentOrElse(
                    result -> {
                        log.info("dailyGoal 변경({}) → 커리큘럼 재생성 (userId={})", request.dailyGoal(), userId);
                        curriculumService.createCurriculum(userId, result);
                    },
                    () -> log.warn("진단 결과 없음 — 커리큘럼 재생성 생략 (userId={})", userId)
            );
        }

        return toResponse(user);
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getDailyGoal(),
                user.getAlarmEnabled(),
                user.getEmailVerified(),
                user.getFontSize()
        );
    }
}
