package com.example.tune_share_hub_backend.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException e) {

        ApiError error;

        if (e.getData() != null) {
            error =  ApiError.of(e.getErrorCode(), e.getMessage());

        } else {
            error = ApiError.of(e.getErrorCode());
        }

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(
            MethodArgumentNotValidException e) {
        List<ValidationError> details = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .toList();

        ApiError error = ApiError.of(ErrorCode.INVALID_REQUEST, details);

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(
            MethodArgumentTypeMismatchException e) {
        ApiError error = ApiError.of(ErrorCode.INVALID_REQUEST,
                List.of(e.getName() + "잘못된 형식입니다."));

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e) {
        ApiError error = ApiError.of(ErrorCode.INVALID_REQUEST, List.of("요청 본문이 필요합니다."));

        return ResponseEntity
                .badRequest()
                .body(error);
    }
}
