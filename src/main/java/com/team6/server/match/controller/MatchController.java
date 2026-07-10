package com.team6.server.match.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.match.dto.MatchRequestDto;
import com.team6.server.match.dto.RingResponse;
import com.team6.server.match.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "링", description = "월간/연간 챔피언 리그 및 매칭 조회 관련 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @Operation(summary = "링 화면 조회 (메인·목록·대결)",
            description = "현재 열린 밸런스 질문, 대결 가능한 본인 기억 리스트, 진행 중인 대결 및 이벤트를 통합 조회합니다.")
    @GetMapping("/ring")
    public ApiResponse<RingResponse> getRingScreen(Authentication authentication) {
        return ApiResponse.success(matchService.getRingScreen(authentication));
    }

    @Operation(summary = "대결 시작", description = "두 에피소드를 선택해 대결을 생성합니다.")
    @PostMapping("/matches")
    public ApiResponse<Long> startMatch(@Valid @RequestBody MatchRequestDto request, Authentication authentication) {
        return ApiResponse.success(matchService.startMatch(authentication, request));
    }

    @Operation(summary = "대결 취소", description = "진행 중인 대결을 취소하고 에피소드 상태를 복구합니다.")
    @DeleteMapping("/matches/{matchId}")
    public ApiResponse<Void> cancelMatch(@PathVariable Long matchId, Authentication authentication) {
        matchService.cancelMatch(authentication, matchId);
        return ApiResponse.noDataSuccess();
    }
}
