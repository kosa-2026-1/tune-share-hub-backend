package com.example.tune_share_hub_backend.dto.searchhistory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "검색어 히스토리 응답")
public class SearchHistoryResponseDto {
    @Schema(description = "검색 히스토리 ID", example = "1")
    private Long historyId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "검색어", example = "아이유")
    private String keyword;

    @Schema(description = "검색 일시", example = "2026-05-14T10:30:00")
    private LocalDateTime createdAt;
}
