package com.example.tune_share_hub_backend.service.searchhistory;

import com.example.tune_share_hub_backend.convert.SearchHistoryConvert;
import com.example.tune_share_hub_backend.dao.searchhistory.SearchHistoryDao;
import com.example.tune_share_hub_backend.dto.searchhistory.SearchHistoryResponseDto;
import com.example.tune_share_hub_backend.entity.searchhistory.SearchHistory;
import com.example.tune_share_hub_backend.validate.SearchHistoryValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchHistoryService {
    private final SearchHistoryDao searchHistoryDao;

    @Value("${search.history.max-count}")
    private int maxSearchHistoryCount;


    @Transactional
    public void saveHistory(SearchHistory searchHistory) {
        SearchHistoryValidator.validateSaveRequest(searchHistory);

        String keyword = searchHistory.getKeyword().trim();
        Long userId = searchHistory.getUserId();

        // 기존 검색어의 생성일 변경
        int updatedRows = searchHistoryDao.updateCreatedAtByUserIdAndKeyword(userId, keyword);

        if(updatedRows == 0){
            // 검색 기록이 없다면 검색어 히스토리 생성
            log.info("Save search history - userId: {}, keyword: {}", userId, keyword);
            searchHistoryDao.insert(userId, keyword);
        }else{
            // 검색어 생성일 변경
            log.info("Update search history createdAt - userId: {}, keyword: {}", userId, keyword);
        }

        // 검색 기록 10개 초과 시 삭제
        searchHistoryDao.deleteExcessHistory(userId, maxSearchHistoryCount);
    }

    public List<SearchHistoryResponseDto> getHistory(Long userId) {
        List<SearchHistory> searchHistoryList = searchHistoryDao.findAllByUserId(userId);
        return searchHistoryList.stream()
                .map(SearchHistoryConvert::toResponseDto)
                .toList();
    }

    @Transactional
    public void deleteHistory(Long userId, Long historyId) {
        SearchHistory searchHistory = searchHistoryDao.findByHistoryId(historyId);
        SearchHistoryValidator.validateSearchHistoryExists(searchHistory);

        // 검색 기록 삭제
        searchHistoryDao.deleteByUserIdAndHistoryId(userId, historyId);
    }
}
