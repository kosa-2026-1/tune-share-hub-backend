package com.example.tune_share_hub_backend.validate;

import com.example.tune_share_hub_backend.entity.searchhistory.SearchHistory;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;

public class SearchHistoryValidator {

    public static void validateSaveRequest(SearchHistory searchHistory) {
        if (searchHistory == null || searchHistory.getUserId() == null
                || searchHistory.getKeyword() == null || searchHistory.getKeyword().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    public static void validateSearchHistoryExists(SearchHistory searchHistory) {
        if (searchHistory == null) {
            throw new CustomException(ErrorCode.SEARCH_HISTORY_NOT_FOUND);
        }
    }
}
