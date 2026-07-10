package com.team6.server.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T> (
        boolean success,
        String code,
        String message,
        T data
) {

    // 데이터가 있는 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "요청에 성공했습니다.", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "SUCCESS", message, data);
    }

    // 데이터 없이 성공 메시지만 보낼 때 (ex: 등록/수정/삭제 완료 시)
    public static ApiResponse<Void> noDataSuccess() {
        return new ApiResponse<>(true, "SUCCESS", "요청에 성공했습니다.", null);
    }

    // 성공 메시지만 커스텀해서 보낼 때
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, "SUCCESS", message, null);
    }

    // 실패 응답
    public static ApiResponse<Void> failure(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}