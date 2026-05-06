package com.example.tune_share_hub_backend.dto.common;

import lombok.Getter;

@Getter
public class ApiResponseDto<T> {
    private final boolean success;
    private final T data;
    private final String message;

    private ApiResponseDto(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponseDto<T> success(T data, String message) {
        return new ApiResponseDto<>(true, data, message);
    }
}
