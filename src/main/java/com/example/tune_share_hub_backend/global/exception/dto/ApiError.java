package com.example.tune_share_hub_backend.global.exception.dto;

import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ApiError {

    private final String code;

    private final String message;

    private final List<?> details;

    public static ApiError of(ErrorCode errorCode) {
        return new ApiError(errorCode.name(), errorCode.getMessage(), List.of());
    }

    public static ApiError of(ErrorCode errorCode, List<?> details) {
        return new ApiError(errorCode.name(), errorCode.getMessage(), details);
    }

    public static ApiError of(ErrorCode errorCode, String message) {
        return new ApiError(
                errorCode.name(),
                message,
                List.of());
    }


}
