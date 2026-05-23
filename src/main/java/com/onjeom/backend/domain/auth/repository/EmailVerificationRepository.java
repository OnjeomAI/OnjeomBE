package com.onjeom.backend.domain.auth.repository;

import com.onjeom.backend.domain.auth.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByUserIdAndIsUsedFalseOrderByCreatedAtDesc(Long userId);

    Optional<EmailVerification> findByOtpCodeAndIsUsedFalse(String otpCode);
}
