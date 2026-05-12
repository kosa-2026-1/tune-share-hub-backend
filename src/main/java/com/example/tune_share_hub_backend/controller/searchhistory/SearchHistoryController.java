package com.example.tune_share_hub_backend.controller.searchhistory;

import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.searchhistory.SearchHistoryRequestDto;
import com.example.tune_share_hub_backend.dto.searchhistory.SearchHistoryResponseDto;
import com.example.tune_share_hub_backend.global.interceptor.AccessTokenCheck;
import com.example.tune_share_hub_backend.global.interceptor.LoginUserId;
import com.example.tune_share_hub_backend.service.searchhistory.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchHistoryController {
    private final SearchHistoryService searchHistoryService;

    @Operation(summary = "검색어 히스토리 저장", description = "검색어 히스토리를 저장합니다.")
    @PostMapping("/history")
    @AccessTokenCheck
    public ApiResponseDto<Void> saveHistory(
            @LoginUserId Long userId,
            @Valid @RequestBody SearchHistoryRequestDto request){
        searchHistoryService.saveHistory(userId, request);
        return ApiResponseDto.success(null, "검색어 히스토리가 저장되었습니다.");
    }

    @Operation(summary = "검색어 히스토리 조회", description = "검색어 히스토리를 조회합니다.")
    @GetMapping("/history")
    @AccessTokenCheck
    public ApiResponseDto<List<SearchHistoryResponseDto>> getHistory(@LoginUserId Long userId) {
        List<SearchHistoryResponseDto> searchHistoryResponseDtoList = searchHistoryService.getHistory(userId);
        return ApiResponseDto.success(searchHistoryResponseDtoList, "검색어 히스토리 조회 성공");
    }

    @Operation(summary = "검색어 히스토리 삭제", description = "검색어 히스토리를 삭제합니다.")
    @DeleteMapping("/history")
    @AccessTokenCheck
    public ApiResponseDto<Void> deleteHistory(
            @LoginUserId Long userId,
            @RequestParam Long historyId) {
        searchHistoryService.deleteHistory(userId, historyId);
        return ApiResponseDto.success(null, "검색어 히스토리가 삭제되었습니다.");
    }
}
