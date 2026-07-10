package com.team6.server.home.controller;

import com.team6.server.global.response.ApiResponse;
import com.team6.server.home.dto.HomeResponse;
import com.team6.server.home.service.HomeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController {
    private final HomeService service;

    public HomeController(HomeService service) { this.service = service; }

    @GetMapping
    public ApiResponse<HomeResponse> get(Authentication authentication) {
        return ApiResponse.success(service.get(authentication));
    }
}
