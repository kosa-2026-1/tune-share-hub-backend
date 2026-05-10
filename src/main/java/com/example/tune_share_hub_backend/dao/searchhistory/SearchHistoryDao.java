package com.example.tune_share_hub_backend.dao.searchhistory;

import com.example.tune_share_hub_backend.entity.searchhistory.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchHistoryDao {
    void insert(@Param("userId") Long userId, @Param("keyword") String keyword);
    void deleteByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
    void deleteExcessHistory(@Param("userId") Long userId);
    List<SearchHistory> findAllByUserId(@Param("userId") Long userId);
}
