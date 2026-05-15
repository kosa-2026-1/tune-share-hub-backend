package com.example.tune_share_hub_backend.global.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "필드 검증 에러")
public class ValidationError {
    @Schema(description = "검증 실패 필드", example = "title")
    private final String field;

    @Schema(description = "검증 실패 메시지", example = "제목은 필수입니다.")
    private final String message;
}
