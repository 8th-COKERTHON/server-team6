package com.team6.server.global.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.global.response.ApiResponse;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
@Component public class RestAccessDeniedHandler implements AccessDeniedHandler {private final ObjectMapper mapper;public RestAccessDeniedHandler(ObjectMapper m){mapper=m;}public void handle(HttpServletRequest r,HttpServletResponse s,AccessDeniedException e)throws IOException{ErrorCode code=ErrorCode.ACCESS_DENIED;s.setStatus(code.getHttpStatus().value());s.setContentType("application/json;charset=UTF-8");mapper.writeValue(s.getWriter(),ApiResponse.failure(code.getCode(),code.getMessage()));}}
