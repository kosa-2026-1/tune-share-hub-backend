package com.example.tune_share_hub_backend.controller.searchhistory;

import com.example.tune_share_hub_backend.convert.SearchHistoryConvert;
import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.searchhistory.SearchHistoryRequestDto;
import com.example.tune_share_hub_backend.dto.searchhistory.SearchHistoryResponseDto;
import com.example.tune_share_hub_backend.global.exception.dto.ApiError;
import com.example.tune_share_hub_backend.global.interceptor.AccessTokenCheck;
import com.example.tune_share_hub_backend.global.interceptor.LoginUserId;
import com.example.tune_share_hub_backend.service.searchhistory.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search History", description = "검색어 히스토리 API")
public class SearchHistoryController {
    private final SearchHistoryService searchHistoryService;

    @Operation(summary = "검색어 히스토리 저장", description = "로그인한 사용자의 검색어 히스토리를 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색어 히스토리 저장 성공"),
            @ApiResponse(responseCode = "400", description = "검색어가 비어 있거나 100자를 초과했습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/history")
    @AccessTokenCheck
    public ApiResponseDto<Void> saveHistory(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @Valid @RequestBody SearchHistoryRequestDto request){
        searchHistoryService.saveHistory(SearchHistoryConvert.toEntity(request, userId));
        return ApiResponseDto.success(null, "검색어 히스토리가 저장되었습니다.");
    }

    @Operation(summary = "검색어 히스토리 조회", description = "로그인한 사용자의 검색어 히스토리를 최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색어 히스토리 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SearchHistoryResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/history")
    @AccessTokenCheck
    public ApiResponseDto<List<SearchHistoryResponseDto>> getHistory(
            @Parameter(hidden = true) @LoginUserId Long userId) {
        List<SearchHistoryResponseDto> searchHistoryResponseDtoList = searchHistoryService.getHistory(userId);
        return ApiResponseDto.success(searchHistoryResponseDtoList, "검색어 히스토리 조회 성공");
    }

    @Operation(summary = "검색어 히스토리 삭제", description = "로그인한 사용자의 특정 검색어 히스토리를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색어 히스토리 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "검색어 히스토리를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/history")
    @AccessTokenCheck
    public ApiResponseDto<Void> deleteHistory(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @Parameter(description = "삭제할 검색 히스토리 ID", example = "1", required = true)
            @RequestParam Long historyId) {
        searchHistoryService.deleteHistory(userId, historyId);
        return ApiResponseDto.success(null, "검색어 히스토리가 삭제되었습니다.");
    }
}
