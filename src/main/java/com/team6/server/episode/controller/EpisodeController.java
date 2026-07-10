package com.team6.server.episode.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.episode.dto.*;
import com.team6.server.episode.service.EpisodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@Validated
@RequestMapping("/api/v1/episodes")
@Tag(name = "Episode", description = "회원 소유 에피소드 등록·조회 및 제목 제안 API")
public class EpisodeController {
    private final EpisodeService service;

    public EpisodeController(EpisodeService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "에피소드 등록", description = "오늘(Asia/Seoul) 또는 과거 날짜의 에피소드를 등록하고 초기 랭킹(제목 점수 0)을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "GLOBAL_400_1(읽을 수 없는 JSON), GLOBAL_400_2(필드 검증 실패), EPISODE_400_1(미래 날짜)", content = @Content(examples = @ExampleObject(value = "{\"success\":false,\"code\":\"EPISODE_400_1\",\"message\":\"에피소드의 날짜가 올바르지 않습니다.\"}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패: AUTH_401_1, AUTH_401_2, AUTH_401_4")
    })
    public ApiResponse<CreateEpisodeResponse> create(@Valid @RequestBody CreateEpisodeRequest request,
                                                     Authentication authentication) {
        return ApiResponse.success("에피소드가 등록되었습니다.", service.create(request, authentication));
    }

    @PostMapping("/title-suggestions")
    @Operation(summary = "에피소드 제목 제안", description = "입력한 내용에서 제목 하나를 생성합니다. 현재 구현은 로컬 제목 제안기를 사용합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "GLOBAL_400_1(읽을 수 없는 JSON), GLOBAL_400_2(필드 검증 실패)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패: AUTH_401_1, AUTH_401_2, AUTH_401_4")
    })
    public ApiResponse<TitleSuggestionResponse> suggestTitle(@Valid @RequestBody TitleSuggestionRequest request,
                                                              Authentication authentication) {
        return ApiResponse.success("제목이 생성되었습니다.", service.suggestTitle(request, authentication));
    }

    @GetMapping("/{episodeId}")
    @Operation(summary = "에피소드 상세 조회", description = "본인 소유 에피소드를 조회합니다. ADMIN은 다른 회원의 에피소드도 조회할 수 있으며, 일반 회원에게 소유권이 없는 에피소드는 존재 여부를 숨기기 위해 404를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패: AUTH_401_1, AUTH_401_2, AUTH_401_4"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "없거나 조회 권한이 없는 에피소드: EPISODE_404_1", content = @Content(examples = @ExampleObject(value = "{\"success\":false,\"code\":\"EPISODE_404_1\",\"message\":\"에피소드를 찾을 수 없습니다.\"}")))
    })
    public ApiResponse<EpisodeDetailResponse> getDetail(@PathVariable Long episodeId,
                                                        Authentication authentication) {
        return ApiResponse.success(service.getDetail(episodeId, authentication));
    }

    @GetMapping
    @Operation(summary = "내 에피소드 목록 조회", description = "현재 회원의 에피소드를 최신순으로 cursor 페이지 조회합니다. status를 생략하면 전체 상태를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "GLOBAL_400_1(잘못된 status 또는 cursor), GLOBAL_400_2(size 범위 위반)", content = @Content(examples = @ExampleObject(value = "{\"success\":false,\"code\":\"GLOBAL_400_1\",\"message\":\"올바르지 않은 cursor입니다.\"}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패: AUTH_401_1, AUTH_401_2, AUTH_401_4")
    })
    public ApiResponse<EpisodeListResponse> getAll(
            @Parameter(description = "에피소드 상태 필터", example = "AVAILABLE") @RequestParam(required = false) String status,
            @Parameter(description = "페이지 크기(1~50)", example = "20") @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @Parameter(description = "이전 응답의 nextCursor. 첫 페이지에서는 생략", example = "MjAyNi0wNy0xMVQxMDozMDowMHwxMjM") @RequestParam(required = false) String cursor,
            Authentication authentication) {
        return ApiResponse.success(service.getAll(status, size, cursor, authentication));
    }

    @GetMapping("/search")
    @Operation(summary = "내 에피소드 검색", description = "현재 회원이 등록한 전체 에피소드를 제목에서 먼저 검색하고, 제목 결과가 없으면 내용에서 검색합니다.")
    public ApiResponse<EpisodeSearchResponse> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            Authentication authentication) {
        return ApiResponse.success(service.search(query, page, size, authentication));
    }
}
