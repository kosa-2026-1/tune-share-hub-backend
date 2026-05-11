package com.example.tune_share_hub_backend.dto.searchhistory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SearchHistoryRequestDto {
    @NotBlank(message = "검색어는 필수입니다")
    @Size(max = 100, message = "검색어는 100자 이하로 입력해주세요")
    private String keyword;
}
