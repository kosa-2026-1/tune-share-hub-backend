package com.example.tune_share_hub_backend.entity.like;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Like {
    private Long likeId;
    private Long playlistId;
    private Long userId;
    private LocalDateTime createdAt;
    private String status;
}
