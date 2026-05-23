package com.onjeom.backend.domain.user.service;

import com.onjeom.backend.domain.user.dto.request.UpdateProfileRequest;
import com.onjeom.backend.domain.user.dto.response.UserProfileResponse;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.domain.user.repository.UserRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return toResponse(user);
    }

    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateProfile(request.nickname(), request.alarmEnabled(), request.dailyGoal());
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
                user.getEmailVerified()
        );
    }
}
