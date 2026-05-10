package com.example.tune_share_hub_backend.dto.searchhistory;

import com.example.tune_share_hub_backend.dto.user.UserResponseDto;
import com.example.tune_share_hub_backend.entity.searchhistory.SearchHistory;
import com.example.tune_share_hub_backend.entity.user.User;
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

    public static SearchHistoryResponseDto from(SearchHistory searchHistory) {
        return SearchHistoryResponseDto.builder()
                .historyId(searchHistory.getHistoryId())
                .userId(searchHistory.getUserId())
                .keyword(searchHistory.getKeyword())
                .createdAt(searchHistory.getCreatedAt())
                .build();
    }
}
