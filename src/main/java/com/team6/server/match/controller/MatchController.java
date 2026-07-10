package com.team6.server.match.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.match.dto.MatchResponse;
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
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    /* 진행할 수 있는 매칭 리스트 조회 API */
    @Operation(
            summary = "진행할 수 있는 매칭 리스트 조회",
            description = "현재 활성화되어 유저가 참여하거나 대결을 진행할 수 있는 리그(STATUS = 'ACTICE') 목록을 반환합니다."
    )
    @GetMapping("/active")
    public ApiResponse<List<MatchResponse>> getActiveLeagues() {
        return ApiResponse.success(matchService.getActiveLeagues());
    }

    /* 앞으로 진행 가능한 매칭 리스트 조회 API */
    @Operation(
            summary = "앞으로 진행 가능한 매칭 리스트 조회",
            description = "아직 오픈되지 않았거나 대기 중인 챔피언 리그(STATUS = 'UPCOMING') 목록을 반환합니다."
    )
    @GetMapping("/upcoming")
    public ApiResponse<List<MatchResponse>> getUpcomingLeagues() {
        return ApiResponse.success(matchService.getUpcomingLeagues());
    }
}
