package com.onjeom.backend.domain.user.controller.api;

import com.onjeom.backend.domain.user.dto.request.UpdateProfileRequest;
import com.onjeom.backend.domain.user.dto.response.UserProfileResponse;
import com.onjeom.backend.global.common.ApiResponse;
import com.onjeom.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "사용자 API")
public interface UserApi {

    @Operation(summary = "내 프로필 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/me")
    ResponseEntity<ApiResponse<?>> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "내 프로필 수정")
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/me")
    ResponseEntity<ApiResponse<?>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request);
}
