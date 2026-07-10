package com.team6.server.global.exception;
import lombok.Getter;
@Getter public class BusinessException extends RuntimeException { private final ErrorCode errorCode; public BusinessException(ErrorCode e){super(e.getMessage());this.errorCode=e;} }
