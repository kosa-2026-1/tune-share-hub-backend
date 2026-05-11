package com.example.tune_share_hub_backend.dto.playlist;

import com.example.tune_share_hub_backend.entity.Playlist;
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
    private String coverImageUrl;
    private int commentCount;
    private LocalDateTime createdAt;
    private List<Object> tracks;

    public static PlaylistResponseDto from(Playlist playlist) {
        return PlaylistResponseDto.builder()
                .playlistId(playlist.getPlaylistId())
                .title(playlist.getTitle())
                .description(playlist.getDescription())
                .publicYn(playlist.getPublicYn())
                .viewCount(playlist.getViewCount())
                .likeCount(playlist.getLikeCount())
                .coverImageUrl(playlist.getCoverImageUrl())
                .commentCount(playlist.getCommentCount())
                .createdAt(playlist.getCreatedAt())
                .build();
    }
}