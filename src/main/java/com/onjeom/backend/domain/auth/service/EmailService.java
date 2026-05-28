package com.onjeom.backend.domain.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    public void sendOtpEmail(String toEmail, String otpCode) {
        // TODO: 메일 설정 후 실제 발송으로 교체
        log.info("[DEV] OTP 메일 발송 생략 - to: {}, code: {}", toEmail, otpCode);
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        // TODO: 메일 설정 후 실제 발송으로 교체
        log.info("[DEV] 비밀번호 재설정 메일 발송 생략 - to: {}, token: {}", toEmail, resetToken);
    }
}
