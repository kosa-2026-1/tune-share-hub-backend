package com.example.tune_share_hub_backend.dto.playlist;

import java.time.LocalDateTime;
import java.util.List;

import com.example.tune_share_hub_backend.dto.music.CommentResponseDto;
import com.example.tune_share_hub_backend.entity.PlaylistTrack;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class PlaylistDetailResponseDto {

    private Long playlistId;
    private String title;
    private String description;
    private String publicYn;
    private int viewCount;
    private int likeCount;
    private String coverImageUrl;
    private int commentCount;
    private LocalDateTime createdAt;

    private List<PlaylistTrack> tracks;
    private List<CommentResponseDto> comments;
}
