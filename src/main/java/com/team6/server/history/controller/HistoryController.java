package com.team6.server.history.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.history.dto.ChampionHistoryItemResponse;
import com.team6.server.history.dto.HistoryHomeResponse;
import com.team6.server.history.dto.MatchHistoryItemResponse;
import com.team6.server.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "기록실", description = "챔피언 기록, 역대 매치 기록 및 기록실 검색 API")
@RestController
@Validated
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController {
    private final HistoryService historyService;

    @Operation(summary = "기록실 홈 조회", description = "검색어가 있으면 제목/내용 기준으로 챔피언 기록과 역대 매치 기록을 함께 필터링합니다.")
    @GetMapping
    public ApiResponse<HistoryHomeResponse> getHome(
            @Parameter(description = "에피소드 제목/내용 검색어") @RequestParam(required = false) String query,
            Authentication authentication) {
        return ApiResponse.success(historyService.getHome(query, authentication));
    }

    @Operation(summary = "역대 챔피언 기록 전체보기", description = "현재 랭킹 점수 기준 상위 에피소드를 챔피언 기록으로 조회합니다.")
    @GetMapping("/champions")
    public ApiResponse<List<ChampionHistoryItemResponse>> getChampionRecords(
            @Parameter(description = "에피소드 제목/내용 검색어") @RequestParam(required = false) String query,
            @Parameter(description = "조회 개수(1~50)") @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            Authentication authentication) {
        return ApiResponse.success(historyService.getChampionRecords(query, size, authentication));
    }

    @Operation(summary = "역대 매치 기록 전체보기", description = "완료된 매치의 양쪽 에피소드와 승/패 결과를 최신순으로 조회합니다.")
    @GetMapping("/matches")
    public ApiResponse<List<MatchHistoryItemResponse>> getMatchRecords(
            @Parameter(description = "에피소드 제목/내용 검색어") @RequestParam(required = false) String query,
            @Parameter(description = "조회 개수(1~50)") @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            Authentication authentication) {
        return ApiResponse.success(historyService.getMatchRecords(query, size, authentication));
    }
}
