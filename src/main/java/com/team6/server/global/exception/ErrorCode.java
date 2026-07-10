package com.team6.server.global.exception;
import org.springframework.http.HttpStatus;
public enum ErrorCode {
 INVALID_INPUT(HttpStatus.BAD_REQUEST,"INVALID_INPUT","입력값이 올바르지 않습니다."), INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED,"INVALID_CREDENTIALS","이메일 또는 비밀번호가 올바르지 않습니다."), INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"INVALID_TOKEN","유효하지 않은 토큰입니다."), UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","인증이 필요합니다."), FORBIDDEN(HttpStatus.FORBIDDEN,"FORBIDDEN","접근 권한이 없습니다."), MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,"MEMBER_NOT_FOUND","회원을 찾을 수 없습니다."), EMAIL_DUPLICATED(HttpStatus.CONFLICT,"EMAIL_DUPLICATED","이미 사용 중인 이메일입니다."), INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL_ERROR","서버 오류가 발생했습니다.");
 public final HttpStatus status; public final String code; public final String message;
 ErrorCode(HttpStatus status,String code,String message){this.status=status;this.code=code;this.message=message;}
}
