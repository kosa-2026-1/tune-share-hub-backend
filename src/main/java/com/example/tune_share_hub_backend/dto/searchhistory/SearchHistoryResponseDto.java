package com.example.tune_share_hub_backend.dto.searchhistory;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SearchHistoryResponseDto {
    private Long historyId;
    private Long userId;
    private String keyword;
    private LocalDateTime createdAt;
}
