package com.team6.server.auth.controller;

import com.team6.server.auth.dto.LoginRequest;
import com.team6.server.auth.dto.LoginResponse;
import com.team6.server.auth.dto.RefreshRequest;
import com.team6.server.auth.dto.SignUpRequest;
import com.team6.server.auth.dto.TokenResponse;
import com.team6.server.auth.service.AuthService;
import com.team6.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<Map<String, Long>> signUp(@Valid @RequestBody SignUpRequest request) {
        return ApiResponse.success("회원가입에 성공했습니다.", Map.of("memberId", service.signUp(request)));
    }

    @PostMapping("/login")
    ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(service.login(request));
    }

    @PostMapping("/refresh")
    ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(service.refresh(request));
    }
}
