package com.team6.server.global.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.server.global.exception.ErrorCode;
import com.team6.server.global.response.ApiResponse;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
@Component public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {private final ObjectMapper mapper;public RestAuthenticationEntryPoint(ObjectMapper m){mapper=m;}public void commence(HttpServletRequest r,HttpServletResponse s,AuthenticationException e)throws IOException{Object value=r.getAttribute(JwtFilter.AUTH_ERROR_ATTRIBUTE);ErrorCode code=value instanceof ErrorCode error?error:ErrorCode.TOKEN_NOT_FOUND;s.setStatus(code.getHttpStatus().value());s.setContentType("application/json;charset=UTF-8");mapper.writeValue(s.getWriter(),ApiResponse.failure(code.getCode(),code.getMessage()));}}
