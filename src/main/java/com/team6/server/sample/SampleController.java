package com.team6.server.sample;

import com.team6.server.global.response.ApiResponse;
import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sample")
public class SampleController {

    @GetMapping("/public")
    public ApiResponse<Map<String, String>> publicApi() {
        return ApiResponse.success(Map.of("message", "미인증 사용자도 호출할 수 있습니다."));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, String>> authenticatedApi(Principal principal) {
        return ApiResponse.success(Map.of(
                "memberId", principal.getName(),
                "message", "인증 사용자만 호출할 수 있습니다."
        ));
    }
}
