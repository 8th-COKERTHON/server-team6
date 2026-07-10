package com.team6.server.match.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.match.dto.ShowSessionResponse;
import com.team6.server.match.dto.ShowSessionProgressResponse;
import com.team6.server.match.service.PlacementService;
import com.team6.server.match.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class PlacementController {
    private final PlacementService service;
    private final ShowService shows;
    public PlacementController(PlacementService service, ShowService shows) { this.service = service; this.shows = shows; }

    @PostMapping("/placements/onboarding")
    @Operation(summary = "최초 온보딩 배치전 시작", description = "서버가 본인의 AVAILABLE/PENDING 에피소드 5개를 자동 선택해 모든 조합인 10경기를 생성합니다. 요청 body는 없습니다.")
    public ApiResponse<ShowSessionProgressResponse> startOnboarding(Authentication authentication) {
        ShowSessionResponse started = service.startOnboarding(authentication);
        return ApiResponse.success("온보딩 배치전이 시작되었습니다.",
                shows.getSession(started.sessionId(), authentication));
    }

    @PostMapping("/episodes/{episodeId}/placement")
    @Operation(summary = "추가 에피소드 배치전 시작", description = "서버가 신규 에피소드와 점수가 가까운 Placement 완료 에피소드 5개를 자동 선택합니다. 요청 body는 없습니다.")
    public ApiResponse<ShowSessionProgressResponse> startAdditional(@PathVariable Long episodeId,
                                                                    Authentication authentication) {
        ShowSessionResponse started = service.startAdditional(episodeId, authentication);
        return ApiResponse.success("추가 에피소드 배치전이 시작되었습니다.",
                shows.getSession(started.sessionId(), authentication));
    }
}
