package com.onjeom.backend.domain.user.controller;

import com.onjeom.backend.domain.user.controller.api.UserApi;
import com.onjeom.backend.domain.user.dto.request.UpdateProfileRequest;
import com.onjeom.backend.domain.user.dto.response.UserProfileResponse;
import com.onjeom.backend.domain.user.service.UserService;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileResponse response = userService.getProfile(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateProfile(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
