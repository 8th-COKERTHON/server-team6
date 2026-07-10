package com.team6.server.match.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.match.dto.AvailableShowResponse;
import com.team6.server.match.dto.ShowSessionResponse;
import com.team6.server.match.dto.ShowSessionProgressResponse;
import com.team6.server.match.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowController {
    private final ShowService service;
    public ShowController(ShowService service) { this.service = service; }

    @GetMapping("/available")
    @Operation(summary = "진행 가능한 Weekly·Monthly Show 조회")
    public ApiResponse<List<AvailableShowResponse>> available(Authentication authentication) {
        return ApiResponse.success(service.available(authentication));
    }

    @PostMapping("/{showId}/sessions")
    @Operation(summary = "Weekly·Monthly Show 참여 시작", description = "본인의 사용 가능한 에피소드로 회원별 대진을 생성합니다.")
    public ApiResponse<ShowSessionProgressResponse> start(@PathVariable Long showId, Authentication authentication) {
        ShowSessionResponse started = service.start(showId, authentication);
        return ApiResponse.success("Show 참여가 시작되었습니다.",
                service.getSession(started.sessionId(), authentication));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Show session 진행 상태와 다음 경기 조회")
    public ApiResponse<ShowSessionProgressResponse> getSession(@PathVariable Long sessionId,
                                                               Authentication authentication) {
        return ApiResponse.success(service.getSession(sessionId, authentication));
    }
}
