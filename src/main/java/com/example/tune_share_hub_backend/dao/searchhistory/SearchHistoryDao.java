package com.example.tune_share_hub_backend.dao.searchhistory;

import com.example.tune_share_hub_backend.entity.searchhistory.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchHistoryDao {
    void insert(@Param("userId") Long userId, @Param("keyword") String keyword);
    void deleteByUserIdAndHistoryId(@Param("userId") Long userId, @Param("historyId") Long historyId);
    void deleteExcessHistory(@Param("userId") Long userId, @Param("maxSearchHistoryCount") int maxSearchHistoryCount);
    List<SearchHistory> findAllByUserId(@Param("userId") Long userId);
    int updateCreatedAtByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
    SearchHistory findByHistoryId(@Param("historyId") Long historyId);
}
