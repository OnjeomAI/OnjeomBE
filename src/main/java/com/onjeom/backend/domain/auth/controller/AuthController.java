package com.onjeom.backend.domain.auth.controller;

import com.onjeom.backend.domain.auth.controller.api.AuthApi;
import com.onjeom.backend.domain.auth.dto.request.LoginRequest;
import com.onjeom.backend.domain.auth.dto.request.SignupRequest;
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
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", null));
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
}
