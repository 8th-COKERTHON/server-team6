package com.team6.server.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String name,
        String email,
        boolean onboardingCompleted,
        LocalDateTime onboardingCompletedAt
) {
}
