package com.example.tune_share_hub_backend.dto.like;

import com.example.tune_share_hub_backend.entity.like.Like;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LikeResponseDto {
    private Long likeId;
    private Long playlistId;
    private Long userId;
    private String status;

    public static LikeResponseDto from(Like like){
        return LikeResponseDto.builder()
                .likeId(like.getLikeId())
                .playlistId(like.getPlaylistId())
                .userId(like.getUserId())
                .status(like.getStatus())
                .build();
    }
}
