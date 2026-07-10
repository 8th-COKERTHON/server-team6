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

@RestController
@Validated
@RequestMapping("/api/v1/episodes")
public class EpisodeController {
    private final EpisodeService service;

    public EpisodeController(EpisodeService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateEpisodeResponse> create(@Valid @RequestBody CreateEpisodeRequest request,
                                                     Authentication authentication) {
        return ApiResponse.success("에피소드가 등록되었습니다.", service.create(request, authentication));
    }

    @PostMapping("/title-suggestions")
    public ApiResponse<TitleSuggestionResponse> suggestTitle(@Valid @RequestBody TitleSuggestionRequest request,
                                                              Authentication authentication) {
        return ApiResponse.success("제목이 생성되었습니다.", service.suggestTitle(request, authentication));
    }

    @GetMapping("/{episodeId}")
    public ApiResponse<EpisodeDetailResponse> getDetail(@PathVariable Long episodeId,
                                                        Authentication authentication) {
        return ApiResponse.success(service.getDetail(episodeId, authentication));
    }

    @GetMapping
    public ApiResponse<EpisodeListResponse> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(required = false) String cursor,
            Authentication authentication) {
        return ApiResponse.success(service.getAll(status, size, cursor, authentication));
    }
}
