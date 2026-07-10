package com.team6.server.match.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.match.dto.RingResponse;
import com.team6.server.match.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "링", description = "월간/연간 챔피언 리그 및 매칭 조회 관련 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    /* 링 화면 조회 API */

    @Operation(
            summary = "링 화면 조회 (메인·목록·대결)",
            description = "현재 열린 밸런스 질문, 대결 가능한 본인 기억 리스트, 진행 중인 대결 및 이벤트를 통합 조회합니다."
    )
    @GetMapping("/ring")
    public ApiResponse<RingResponse> getRingScreen() {
        Long mockMemberId = 1L;
        return ApiResponse.success(matchService.getRingScreen(mockMemberId));
    }
}
