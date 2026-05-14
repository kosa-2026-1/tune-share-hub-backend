package com.example.tune_share_hub_backend.global.exception.dto;

import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Schema(description = "API 에러 응답")
public class ApiError {

    @Schema(description = "에러 코드", example = "INVALID_REQUEST")
    private final String code;

    @Schema(description = "에러 메시지", example = "입력값이 올바르지 않습니다.")
    private final String message;

    @Schema(description = "상세 에러 목록")
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
