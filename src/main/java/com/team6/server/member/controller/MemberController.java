package com.team6.server.member.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.member.dto.MemberMeResponse;
import com.team6.server.member.dto.OnboardingStatusResponse;
import com.team6.server.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me")
@Tag(name = "Member", description = "인증된 회원의 프로필과 온보딩 상태 API")
public class MemberController {
    private final MemberService service;

    public MemberController(MemberService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "내 정보 조회", description = "Bearer 토큰의 회원 ID로 현재 회원 정보와 온보딩 완료 여부를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패: AUTH_401_1(유효하지 않은 토큰), AUTH_401_2(만료된 액세스 토큰), AUTH_401_4(토큰 없음)", content = @Content(examples = @ExampleObject(value = "{\"success\":false,\"code\":\"AUTH_401_4\",\"message\":\"인증 토큰이 존재하지 않습니다.\"}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "토큰의 회원을 찾을 수 없음: MEMBER_404_1", content = @Content(examples = @ExampleObject(value = "{\"success\":false,\"code\":\"MEMBER_404_1\",\"message\":\"회원을 찾을 수 없습니다.\"}")))
    })
    public ApiResponse<MemberMeResponse> getMe(Authentication authentication) {
        return ApiResponse.success(service.getMe(authentication));
    }

    @GetMapping("/status")
    @Operation(summary = "온보딩 진행 상태 조회", description = "서버가 관리하는 온보딩 상태와 등록 에피소드 및 배치전 진행 수를 조회합니다. 배치전 기능이 활성화되기 전에는 경기 수가 0입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패: AUTH_401_1, AUTH_401_2, AUTH_401_4"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "토큰의 회원을 찾을 수 없음: MEMBER_404_1")
    })
    public ApiResponse<OnboardingStatusResponse> getOnboardingStatus(Authentication authentication) {
        return ApiResponse.success(service.getOnboardingStatus(authentication));
    }

    @PostMapping("/onboarding/complete")
    @Operation(summary = "온보딩 완료", description = "현재 회원의 온보딩 완료 시각을 기록합니다. 이미 완료된 경우 기존 완료 시각을 유지합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "완료 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패: AUTH_401_1, AUTH_401_2, AUTH_401_4", content = @Content(examples = @ExampleObject(value = "{\"success\":false,\"code\":\"AUTH_401_4\",\"message\":\"인증 토큰이 존재하지 않습니다.\"}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음: MEMBER_404_1", content = @Content(examples = @ExampleObject(value = "{\"success\":false,\"code\":\"MEMBER_404_1\",\"message\":\"회원을 찾을 수 없습니다.\"}")))
    })
    public ApiResponse<MemberMeResponse> completeOnboarding(Authentication authentication) {
        return ApiResponse.success("온보딩이 완료되었습니다.", service.completeOnboarding(authentication));
    }
}
