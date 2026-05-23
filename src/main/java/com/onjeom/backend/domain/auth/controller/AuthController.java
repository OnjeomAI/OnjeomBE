package com.onjeom.backend.domain.auth.controller;

import com.onjeom.backend.domain.auth.controller.api.AuthApi;
import com.onjeom.backend.domain.auth.dto.request.*;
import com.onjeom.backend.domain.auth.dto.response.LoginResponse;
import com.onjeom.backend.domain.auth.dto.response.TokenResponse;
import com.onjeom.backend.domain.auth.service.AuthService;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다. 이메일을 확인해주세요.", null));
    }

    @Override
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다.", null));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @PostMapping("/token/reissue")
    public ResponseEntity<ApiResponse<?>> reissueToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("Authorization").substring(7);
        TokenResponse response = authService.reissueToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization").substring(7);
        authService.logout(userDetails.getUserId(), accessToken);
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다.", null));
    }

    @Override
    @PostMapping("/logout/all")
    public ResponseEntity<ApiResponse<?>> logoutAllDevices(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization").substring(7);
        authService.logoutAllDevices(userDetails.getUserId(), accessToken);
        return ResponseEntity.ok(ApiResponse.success("전체 기기에서 로그아웃되었습니다.", null));
    }

    @Override
    @PostMapping("/password/reset-request")
    public ResponseEntity<ApiResponse<?>> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 이메일을 발송했습니다.", null));
    }

    @Override
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody PasswordResetDto request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다.", null));
    }
}
