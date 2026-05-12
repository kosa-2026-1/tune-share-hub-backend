package com.example.tune_share_hub_backend.convert;

import com.example.tune_share_hub_backend.dto.searchhistory.SearchHistoryRequestDto;
import com.example.tune_share_hub_backend.dto.searchhistory.SearchHistoryResponseDto;
import com.example.tune_share_hub_backend.entity.searchhistory.SearchHistory;

public class SearchHistoryConvert {

    public static SearchHistory toEntity(SearchHistoryRequestDto request, Long userId) {
        if (request == null) {
            return null;
        }

        return SearchHistory.builder()
                .userId(userId)
                .keyword(request.getKeyword())
                .build();
    }

    public static SearchHistoryResponseDto toResponseDto(SearchHistory searchHistory) {
        return SearchHistoryResponseDto.builder()
                .historyId(searchHistory.getHistoryId())
                .userId(searchHistory.getUserId())
                .keyword(searchHistory.getKeyword())
                .createdAt(searchHistory.getCreatedAt())
                .build();
    }
}
