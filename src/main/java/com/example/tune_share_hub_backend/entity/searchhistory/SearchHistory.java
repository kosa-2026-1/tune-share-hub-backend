package com.example.tune_share_hub_backend.entity.searchhistory;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {
    private Long historyId;
    private Long userId;
    private String keyword;
    private LocalDateTime createdAt;
}
