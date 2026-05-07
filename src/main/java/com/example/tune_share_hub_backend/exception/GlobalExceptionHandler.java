package com.example.tune_share_hub_backend.exception;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "errorCode", "INVALID_INPUT",
                "message", message
        ));
    }

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<?> handleCustomException(CustomException e) {
        return ResponseEntity.status(getStatus(e.getErrorCode()))
                .body(Map.of(
                        "success", false,
                        "errorCode", e.getErrorCode(),
                        "message", e.getMessage()
                ));
    }

    private int getStatus(String errorCode) {
        return switch (errorCode) {
            case "UNAUTHORIZED" -> 401;
            case "FORBIDDEN" -> 403;
            case "PLAYLIST_NOT_FOUND" -> 404;
            default -> 400;
        };
    }
}