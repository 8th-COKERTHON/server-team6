package com.team6.server.auth;
import jakarta.validation.constraints.*;
public final class AuthDtos {private AuthDtos(){}
 public record SignUpRequest(@NotBlank @Email String email,@NotBlank @Size(min=8,max=72) String password,@NotBlank @Size(max=50) String name){}
 public record LoginRequest(@NotBlank @Email String email,@NotBlank String password){}
 public record RefreshRequest(@NotBlank String refreshToken){}
 public record TokenResponse(String accessToken,String refreshToken,String tokenType,long expiresIn){}
}
