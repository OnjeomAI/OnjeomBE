package com.onjeom.backend.domain.auth.service;

import com.onjeom.backend.domain.auth.dto.request.*;
import com.onjeom.backend.domain.auth.dto.response.LoginResponse;
import com.onjeom.backend.domain.auth.dto.response.TokenResponse;
import com.onjeom.backend.domain.auth.entity.EmailVerification;
import com.onjeom.backend.domain.auth.entity.RefreshToken;
import com.onjeom.backend.domain.auth.entity.TokenBlacklist;
import com.onjeom.backend.domain.auth.enums.TokenBlacklistReason;
import com.onjeom.backend.domain.auth.repository.EmailVerificationRepository;
import com.onjeom.backend.domain.auth.repository.RefreshTokenRepository;
import com.onjeom.backend.domain.auth.repository.TokenBlacklistRepository;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.domain.user.enums.UserRole;
import com.onjeom.backend.domain.user.repository.UserRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import com.onjeom.backend.global.security.jwt.JwtProvider;
import com.onjeom.backend.global.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        // TODO: 프로덕션 배포 전 이메일 인증 로직 주석 해제
        User user = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .nickname(request.nickname())
                .role(UserRole.STUDENT)
                .emailVerified(true)
                .dailyGoal(5)
                .alarmEnabled(true)
                .build();
        userRepository.save(user);

//        SecureRandom random = new SecureRandom();
//        String otpCode = String.valueOf(100000 + random.nextInt(900000));
//
//        EmailVerification verification = EmailVerification.builder()
//                .userId(user.getId())
//                .otpCode(otpCode)
//                .isUsed(false)
//                .expiresAt(LocalDateTime.now().plusMinutes(10))
//                .build();
//        emailVerificationRepository.save(verification);
//
//        emailService.sendOtpEmail(user.getEmail(), otpCode);
    }

    public void verifyEmail(EmailVerifyRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        EmailVerification verification = emailVerificationRepository
                .findTopByUserIdAndIsUsedFalseOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_OTP));

        if (verification.isExpired()) {
            throw new CustomException(ErrorCode.EXPIRED_OTP);
        }

        if (!verification.getOtpCode().equals(request.otpCode())) {
            throw new CustomException(ErrorCode.INVALID_OTP);
        }

        verification.use();
        user.verifyEmail();
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!user.getEmailVerified()) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        RefreshToken tokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(HashUtil.sha256(refreshToken))
                .deviceInfo(null)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(tokenEntity);

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                accessToken,
                refreshToken
        );
    }

    public TokenResponse reissueToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String tokenHash = HashUtil.sha256(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        refreshTokenRepository.deleteByTokenHash(tokenHash);

        Long userId = jwtProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtProvider.generateAccessToken(userId, user.getRole().name());
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);

        RefreshToken newTokenEntity = RefreshToken.builder()
                .userId(userId)
                .tokenHash(HashUtil.sha256(newRefreshToken))
                .deviceInfo(storedToken.getDeviceInfo())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(newTokenEntity);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public void logout(Long userId, String accessToken) {
        TokenBlacklist blacklist = TokenBlacklist.builder()
                .userId(userId)
                .tokenHash(HashUtil.sha256(accessToken))
                .reason(TokenBlacklistReason.LOGOUT)
                .invalidatedAt(LocalDateTime.now())
                .build();
        tokenBlacklistRepository.save(blacklist);
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    public void logoutAllDevices(Long userId, String accessToken) {
        TokenBlacklist blacklist = TokenBlacklist.builder()
                .userId(userId)
                .tokenHash(HashUtil.sha256(accessToken))
                .reason(TokenBlacklistReason.FORCE_LOGOUT)
                .invalidatedAt(LocalDateTime.now())
                .build();
        tokenBlacklistRepository.save(blacklist);
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    public void requestPasswordReset(PasswordResetRequestDto request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null) {
            return;
        }

        String resetToken = UUID.randomUUID().toString();

        EmailVerification verification = EmailVerification.builder()
                .userId(user.getId())
                .otpCode(resetToken)
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
        emailVerificationRepository.save(verification);

        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    public void resetPassword(PasswordResetDto request) {
        EmailVerification verification = emailVerificationRepository
                .findByOtpCodeAndIsUsedFalse(request.token())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_OTP));

        if (verification.isExpired()) {
            throw new CustomException(ErrorCode.EXPIRED_OTP);
        }

        User user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        verification.use();

        refreshTokenRepository.deleteAllByUserId(user.getId());

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .userId(user.getId())
                .tokenHash(HashUtil.sha256(request.token()))
                .reason(TokenBlacklistReason.PASSWORD_RESET)
                .invalidatedAt(LocalDateTime.now())
                .build();
        tokenBlacklistRepository.save(blacklist);
    }
}
