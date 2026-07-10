package com.team6.server.member.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.member.dto.MemberMeResponse;
import com.team6.server.member.service.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me")
public class MemberController {
    private final MemberService service;

    public MemberController(MemberService service) { this.service = service; }

    @GetMapping
    public ApiResponse<MemberMeResponse> getMe(Authentication authentication) {
        return ApiResponse.success(service.getMe(authentication));
    }

    @PostMapping("/onboarding/complete")
    public ApiResponse<MemberMeResponse> completeOnboarding(Authentication authentication) {
        return ApiResponse.success("온보딩이 완료되었습니다.", service.completeOnboarding(authentication));
    }
}
