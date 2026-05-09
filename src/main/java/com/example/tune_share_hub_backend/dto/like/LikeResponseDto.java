package com.example.tune_share_hub_backend.dto.like;

import com.example.tune_share_hub_backend.entity.like.Like;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LikeResponseDto {
    private Long playlistId;
    private Long userId;
    private String status;
    private int totalLikeCount;
}
