package com.example.tune_share_hub_backend.controller.searchhistory;

import com.example.tune_share_hub_backend.dto.auth.LoginRequestDto;
import com.example.tune_share_hub_backend.dto.searchhistory.SearchHistoryRequestDto;
import com.example.tune_share_hub_backend.dto.searchhistory.SearchHistoryResponseDto;
import com.example.tune_share_hub_backend.global.interceptor.AccessTokenCheck;
import com.example.tune_share_hub_backend.global.interceptor.LoginUserId;
import com.example.tune_share_hub_backend.service.searchhistory.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchHistoryController {
    private final SearchHistoryService searchHistoryService;

    @Operation(summary = "검색어 히스토리 저장", description = "검색어 히스토리를 저장합니다.")
    @PostMapping("/history")
    @AccessTokenCheck
    public ResponseEntity<SearchHistoryResponseDto> saveHistory(
            @LoginUserId Long userId,
            @Valid @RequestBody SearchHistoryRequestDto request){
        SearchHistoryResponseDto searchHistoryResponseDto = searchHistoryService.saveHistory(userId, request);
        return ResponseEntity.ok(searchHistoryResponseDto);
    }
}
