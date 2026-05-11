package com.example.tune_share_hub_backend.dto.playlist;

import com.example.tune_share_hub_backend.dto.music.CommentResponseDto;
import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.entity.PlaylistTrack;
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

    public static PlaylistDetailResponseDto from(Playlist p, List<PlaylistTrack> tracks,
                                                 List<CommentResponseDto> comments) {
        return PlaylistDetailResponseDto.builder()
                .playlistId(p.getPlaylistId())
                .title(p.getTitle())
                .description(p.getDescription())
                .publicYn(p.getPublicYn())
                .viewCount(p.getViewCount())
                .likeCount(p.getLikeCount())
                .coverImageUrl(p.getCoverImageUrl())
                .commentCount(p.getCommentCount())
                .createdAt(p.getCreatedAt())
                .tracks(tracks)
                .comments(comments)
                .build();
    }
}
