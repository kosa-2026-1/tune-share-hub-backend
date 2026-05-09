package com.example.tune_share_hub_backend.service.searchhistory;

import com.example.tune_share_hub_backend.dao.searchhistory.SearchHistoryDao;
import com.example.tune_share_hub_backend.dto.searchhistory.SearchHistoryRequestDto;
import com.example.tune_share_hub_backend.dto.searchhistory.SearchHistoryResponseDto;
import com.example.tune_share_hub_backend.entity.searchhistory.SearchHistory;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchHistoryService {
    private final SearchHistoryDao searchHistoryDao;

    @Transactional
    public SearchHistoryResponseDto saveHistory(Long userId, SearchHistoryRequestDto request) {
        String keyword = request.getKeyword();

        if (keyword == null || keyword.trim().isBlank()) {
            throw new CustomException(ErrorCode.SEARCH_HISTORY_INVALID_KEYWORD);
        }

        keyword = keyword.trim();

        // 검색 기록 삭제
        searchHistoryDao.deleteByUserIdAndKeyword(userId, keyword);

        // 검색 기록 저장
        log.info("Save search history - userId: {}, keyword: {}", userId, keyword);
        searchHistoryDao.insert(userId, keyword);

        // 검색 기록 10개 초과 시 삭제
        searchHistoryDao.deleteExcessHistory(userId);

        return SearchHistoryResponseDto.builder()
                .userId(userId)
                .keyword(keyword)
                .build();
    }
}
