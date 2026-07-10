package com.team6.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "GLOBAL_400_1", "잘못된 입력값입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "GLOBAL_400_2", "입력값 검증에 실패했습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "GLOBAL_400_3", "필수 요청값이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "GLOBAL_401_1", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "GLOBAL_403_1", "접근 권한이 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "GLOBAL_404_1", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "GLOBAL_405_1", "지원하지 않는 HTTP 메서드입니다."),
    CONFLICT(HttpStatus.CONFLICT, "GLOBAL_409_1", "요청이 현재 상태와 충돌합니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_500_1", "서버 내부 오류가 발생했습니다."),

    AI_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "AI_502_1", "AI 제목 생성 응답을 처리할 수 없습니다."),
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_503_1", "AI 제목 생성 서비스를 일시적으로 사용할 수 없습니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "유효하지 않은 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "액세스 토큰이 만료되었습니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_3", "리프레시 토큰이 만료되었습니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_401_4", "인증 토큰이 존재하지 않습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_401_5", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_403_1", "해당 요청에 대한 권한이 없습니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404_1", "회원을 찾을 수 없습니다."),
    MEMBER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "MEMBER_409_1", "이미 사용 중인 이메일입니다."),
    MEMBER_LOGIN_ID_DUPLICATED(HttpStatus.CONFLICT, "MEMBER_409_2", "이미 사용 중인 아이디입니다."),
    MEMBER_NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "MEMBER_409_3", "이미 사용 중인 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER_400_1", "비밀번호 형식이 올바르지 않습니다."),
    CURRENT_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER_400_2", "현재 비밀번호가 일치하지 않습니다."),
    MEMBER_ALREADY_WITHDRAWN(HttpStatus.CONFLICT, "MEMBER_409_4", "이미 탈퇴한 회원입니다."),

    EPISODE_NOT_FOUND(HttpStatus.NOT_FOUND, "EPISODE_404_1", "에피소드를 찾을 수 없습니다."),
    INVALID_EPISODE_DATE(HttpStatus.BAD_REQUEST, "EPISODE_400_1", "에피소드의 날짜가 올바르지 않습니다."),
    PLACEMENT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "EPISODE_409_1", "이미 배치전을 완료했습니다."),
    INSUFFICIENT_PLACEMENT_EPISODES(HttpStatus.BAD_REQUEST, "EPISODE_400_2", "배치전에 필요한 에피소드가 부족합니다."),
    EPISODE_ALREADY_MATCHED(HttpStatus.CONFLICT, "EPISODE_409_2", "이미 해당 에피소드와 매치를 진행했습니다."),

    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH_404_1", "매치를 찾을 수 없습니다."),
    INVALID_MATCH_RESULT(HttpStatus.BAD_REQUEST, "MATCH_400_1", "올바르지 않은 매치 결과입니다."),
    MATCH_ALREADY_COMPLETED(HttpStatus.CONFLICT, "MATCH_409_1", "이미 완료된 매치입니다."),
    SAME_EPISODE_MATCH(HttpStatus.BAD_REQUEST, "MATCH_400_2", "동일한 에피소드끼리는 매치할 수 없습니다."),
    AVAILABLE_MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH_404_2", "진행 가능한 매치가 없습니다."),
    RING_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH_404_3", "링 참여를 찾을 수 없습니다."),
    RING_SESSION_ALREADY_STARTED(HttpStatus.CONFLICT, "MATCH_409_2", "이미 해당 링에 참여했습니다."),
    INSUFFICIENT_RING_EPISODES(HttpStatus.BAD_REQUEST, "MATCH_400_3", "링 진행에 필요한 에피소드가 부족합니다."),
    RING_ROUND_OUT_OF_ORDER(HttpStatus.CONFLICT, "MATCH_409_3", "현재 진행할 수 있는 라운드가 아닙니다."),

    WEEKLY_SHOW_NOT_FOUND(HttpStatus.NOT_FOUND, "SHOW_404_1", "Weekly Show를 찾을 수 없습니다."),
    WEEKLY_SHOW_ALREADY_COMPLETED(HttpStatus.CONFLICT, "SHOW_409_1", "이번 Weekly Show를 이미 완료했습니다."),
    WEEKLY_SHOW_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "SHOW_400_1", "아직 Weekly Show를 진행할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
