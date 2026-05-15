package com.example.tune_share_hub_backend.dto.searchhistory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "검색어 히스토리 저장 요청")
public class SearchHistoryRequestDto {
    @NotBlank(message = "검색어는 필수입니다")
    @Size(max = 100, message = "검색어는 100자 이하로 입력해주세요")
    @Schema(description = "검색어", example = "아이유", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyword;
}
