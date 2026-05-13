package com.example.tune_share_hub_backend.service.searchhistory;

import com.example.tune_share_hub_backend.convert.SearchHistoryConvert;
import com.example.tune_share_hub_backend.dao.searchhistory.SearchHistoryDao;
import com.example.tune_share_hub_backend.dto.searchhistory.SearchHistoryResponseDto;
import com.example.tune_share_hub_backend.entity.searchhistory.SearchHistory;
import com.example.tune_share_hub_backend.validate.SearchHistoryValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        int updatedRows = searchHistoryDao.updateCreatedAtByUserIdAndKeyword(userId, keyword);
        if(updatedRows == 0){
            searchHistoryDao.insert(userId, keyword);
        }

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
        searchHistoryDao.deleteByUserIdAndHistoryId(userId, historyId);
    }
}
