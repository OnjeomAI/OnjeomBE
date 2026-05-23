package com.onjeom.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[온점] 이메일 인증 코드");
        message.setText("인증 코드: " + otpCode + " (10분 이내 입력)");
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[온점] 비밀번호 재설정");
        message.setText("아래 링크를 클릭하여 비밀번호를 재설정하세요.\n"
                + "http://localhost:3000/reset-password?token=" + resetToken);
        mailSender.send(message);
    }
}
