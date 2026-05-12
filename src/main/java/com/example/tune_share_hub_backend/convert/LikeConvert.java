package com.example.tune_share_hub_backend.convert;

import com.example.tune_share_hub_backend.dto.like.LikeResponseDto;
import com.example.tune_share_hub_backend.entity.Playlist;

public class LikeConvert {

    public static LikeResponseDto toResponseDto(Long playlistId, Long userId, String status, Playlist playlist) {
        return LikeResponseDto.builder()
                .playlistId(playlistId)
                .userId(userId)
                .status(status)
                .totalLikeCount(playlist.getLikeCount())
                .build();
    }
}
