package com.team6.server.home.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.home.dto.HomeResponse;
import com.team6.server.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@Tag(name = "Home", description = "인증된 회원의 홈 요약 API")
public class HomeController {
    private final HomeService service;

    public HomeController(HomeService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "홈 조회", description = "서비스 시간대(Asia/Seoul)의 오늘 날짜, 사용 가능한 에피소드 수, 오늘 작성한 최신 에피소드와 예정 이벤트를 반환합니다. 현재 구현에서 canStartMatch는 false, upcomingEvents는 빈 배열입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패: AUTH_401_1, AUTH_401_2, AUTH_401_4", content = @Content(examples = @ExampleObject(value = "{\"success\":false,\"code\":\"AUTH_401_4\",\"message\":\"인증 토큰이 존재하지 않습니다.\"}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "토큰의 회원을 찾을 수 없음: MEMBER_404_1", content = @Content(examples = @ExampleObject(value = "{\"success\":false,\"code\":\"MEMBER_404_1\",\"message\":\"회원을 찾을 수 없습니다.\"}")))
    })
    public ApiResponse<HomeResponse> get(Authentication authentication) {
        return ApiResponse.success(service.get(authentication));
    }
}
