package com.example.tune_share_hub_backend.exception;

import com.example.tune_share_hub_backend.dto.common.ApiErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서비스나 인터셉터에서 발생시킨 ApiException을 공통 실패 응답 형식으로 변환한다.
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponseDto> handleApiException(ApiException exception) {
        ApiErrorResponseDto response = new ApiErrorResponseDto(exception.getErrorCode(), exception.getMessage());
        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    // 예상하지 못한 오류도 JSON 형식으로 응답하기 위한 마지막 안전망이다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(Exception exception) {
        ApiErrorResponseDto response = new ApiErrorResponseDto("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
