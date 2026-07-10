package com.team6.server.matching.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.matching.dto.*;
import com.team6.server.matching.service.RingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/ring")
public class RingController {
    private final RingService service;

    public RingController(RingService service) { this.service = service; }

    @GetMapping("/events")
    public ApiResponse<RingEventListResponse> events(Authentication authentication) {
        return ApiResponse.success(service.getEvents(authentication));
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RingSessionResponse> start(@Valid @RequestBody StartRingSessionRequest request,
                                                  Authentication authentication) {
        return ApiResponse.success("링 참여가 시작되었습니다.", service.start(request, authentication));
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<RingSessionResponse> session(@PathVariable Long sessionId, Authentication authentication) {
        return ApiResponse.success(service.getSession(sessionId, authentication));
    }

    @PostMapping("/sessions/{sessionId}/rounds/{roundNo}/result")
    public ApiResponse<RingRoundResultResponse> select(@PathVariable Long sessionId,
                                                       @PathVariable @Min(1) int roundNo,
                                                       @Valid @RequestBody SelectRingWinnerRequest request,
                                                       Authentication authentication) {
        return ApiResponse.success("라운드 선택이 완료되었습니다.",
                service.selectWinner(sessionId, roundNo, request, authentication));
    }
}
