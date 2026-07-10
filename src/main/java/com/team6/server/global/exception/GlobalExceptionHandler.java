package com.team6.server.global.exception;
import com.team6.server.global.response.ApiResponse;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class GlobalExceptionHandler {
 @ExceptionHandler(BusinessException.class) ResponseEntity<ApiResponse<Void>> business(BusinessException e){var x=e.getErrorCode();return ResponseEntity.status(x.status).body(ApiResponse.failure(x.code,x.message));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException e){String m=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).collect(Collectors.joining(", "));return ResponseEntity.badRequest().body(ApiResponse.failure(ErrorCode.INVALID_INPUT.code,m));}
 @ExceptionHandler(Exception.class) ResponseEntity<ApiResponse<Void>> unknown(Exception e){return ResponseEntity.internalServerError().body(ApiResponse.failure(ErrorCode.INTERNAL_ERROR.code,ErrorCode.INTERNAL_ERROR.message));}
}
