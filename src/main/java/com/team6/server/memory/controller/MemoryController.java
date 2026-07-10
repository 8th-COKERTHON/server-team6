package com.team6.server.memory.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.memory.dto.*;
import com.team6.server.memory.service.MemoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/memories")
public class MemoryController {
    private final MemoryService service;

    public MemoryController(MemoryService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateMemoryResponse> create(@Valid @RequestBody CreateMemoryRequest request,
                                                     Authentication authentication) {
        return ApiResponse.success("기억이 등록되었습니다.", service.create(request, authentication));
    }

    @PostMapping("/title-suggestions")
    public ApiResponse<TitleSuggestionResponse> suggestTitle(@Valid @RequestBody TitleSuggestionRequest request,
                                                              Authentication authentication) {
        return ApiResponse.success("제목이 생성되었습니다.", service.suggestTitle(request, authentication));
    }

    @GetMapping("/{memoryId}")
    public ApiResponse<MemoryDetailResponse> getDetail(@PathVariable Long memoryId,
                                                        Authentication authentication) {
        return ApiResponse.success(service.getDetail(memoryId, authentication));
    }
}
