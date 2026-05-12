package com.example.tune_share_hub_backend.convert;

import com.example.tune_share_hub_backend.dto.music.CommentResponseDto;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistDetailResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.entity.Playlist;

import java.util.List;

public class PlaylistConvert {

    public static PlaylistDetailResponseDto toDetailResponseDto(Playlist p,
                                                                List<PlaylistTrackResponseDto> tracks,
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

    public static PlaylistResponseDto toResponseDto(Playlist playlist) {
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