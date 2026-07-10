package com.team6.server.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(boolean success, String code, String message, T data) {
    public static <T> ApiResponse<T> success(T data) { return new ApiResponse<>(true, "SUCCESS", "요청에 성공했습니다.", data); }
    public static <T> ApiResponse<T> success(String message, T data) { return new ApiResponse<>(true, "SUCCESS", message, data); }
    public static ApiResponse<Void> failure(String code, String message) { return new ApiResponse<>(false, code, message, null); }
}
