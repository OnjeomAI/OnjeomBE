package com.onjeom.backend.domain.auth.controller.api;

import com.onjeom.backend.domain.auth.dto.request.*;
import com.onjeom.backend.domain.auth.dto.response.LoginResponse;
import com.onjeom.backend.domain.auth.dto.response.TokenResponse;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    ResponseEntity<ApiResponse<?>> signup(@Valid @RequestBody SignupRequest request);

    @Operation(summary = "이메일 OTP 인증")
    @PostMapping("/email/verify")
    ResponseEntity<ApiResponse<?>> verifyEmail(@Valid @RequestBody EmailVerifyRequest request);

    @Operation(summary = "로그인")
    @PostMapping("/login")
    ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "토큰 재발급")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/token/reissue")
    ResponseEntity<ApiResponse<?>> reissueToken(HttpServletRequest request);

    @Operation(summary = "로그아웃")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/logout")
    ResponseEntity<ApiResponse<?>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request);

    @Operation(summary = "전체 기기 로그아웃")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/logout/all")
    ResponseEntity<ApiResponse<?>> logoutAllDevices(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request);

    @Operation(summary = "비밀번호 재설정 요청")
    @PostMapping("/password/reset-request")
    ResponseEntity<ApiResponse<?>> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto request);

    @Operation(summary = "비밀번호 재설정")
    @PostMapping("/password/reset")
    ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody PasswordResetDto request);
}
