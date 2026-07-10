package com.team6.server.auth;
import static com.team6.server.auth.AuthDtos.*;
import com.team6.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/auth") public class AuthController {
 private final AuthService service;public AuthController(AuthService s){service=s;}
 @PostMapping("/signup") @ResponseStatus(HttpStatus.CREATED) ApiResponse<Map<String,Long>> signUp(@Valid @RequestBody SignUpRequest r){return ApiResponse.success("회원가입에 성공했습니다.",Map.of("memberId",service.signUp(r)));}
 @PostMapping("/login") ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest r){return ApiResponse.success(service.login(r));}
 @PostMapping("/refresh") ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest r){return ApiResponse.success(service.refresh(r));}
}
