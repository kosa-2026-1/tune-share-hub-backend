package com.example.tune_share_hub_backend.dto.playlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class PlaylistResponseDto {

    private Long playlistId;
    private String title;
    private String description;
    private String publicYn;
    private int viewCount;
    private int likeCount;
    private int trackCount;
    private String coverImageUrl;
    private int commentCount;
    private LocalDateTime createdAt;
    private List<String> tags;


}