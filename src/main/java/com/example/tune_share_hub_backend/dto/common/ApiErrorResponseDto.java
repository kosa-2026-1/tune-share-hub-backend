package com.example.tune_share_hub_backend.dto.common;

import lombok.Getter;

@Getter
public class ApiErrorResponseDto {
    private final boolean success = false;
    private final String errorCode;
    private final String message;

    public ApiErrorResponseDto(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
