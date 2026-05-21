package com.ansj.delivery.user.controller;

import com.ansj.delivery.common.response.ApiResponse;
import com.ansj.delivery.user.dto.LoginRequest;
import com.ansj.delivery.user.dto.SignUpRequest;
import com.ansj.delivery.user.dto.TokenResponse;
import com.ansj.delivery.user.dto.UserResponse;
import com.ansj.delivery.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return ApiResponse.ok("회원가입이 완료되었습니다.", userService.signUp(request));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(userService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(userService.getMe(UUID.fromString(userId)));
    }
}
