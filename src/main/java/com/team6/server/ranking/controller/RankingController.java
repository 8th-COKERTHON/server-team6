package com.team6.server.ranking.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.ranking.dto.RankingListResponse;
import com.team6.server.ranking.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/rankings")
public class RankingController {
    private final RankingService service;
    public RankingController(RankingService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "전체 에피소드 랭킹 조회", description = "점수 내림차순으로 에피소드 제목·점수·경쟁 순위·칭호를 조회합니다.")
    public ApiResponse<RankingListResponse> getAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            Authentication authentication) {
        return ApiResponse.success(service.getAll(page, size, authentication));
    }
}
