package com.team6.server.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MemberMeResponse(Long memberId, String name, String email, boolean onboardingCompleted,
                               LocalDateTime onboardingCompletedAt) { }
