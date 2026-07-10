package com.team6.server.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    // 기본 에러 메시지 사용 시
    public BusinessException(ErrorCode e) {
        super(e.getMessage());
        this.errorCode = e;
    }

    // 🔥 완성: 상황에 맞는 구체적인 커스텀 메시지가 필요할 때 사용
    public BusinessException(ErrorCode e, String customMessage) {
        super(customMessage);
        this.errorCode = e;
    }
}