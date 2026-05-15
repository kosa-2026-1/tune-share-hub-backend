package com.example.tune_share_hub_backend.convert;

import com.example.tune_share_hub_backend.dto.playlist.CommentResponseDto;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistDetailResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.entity.Playlist;

import java.util.List;

public class PlaylistConvert {

    private static final String DEFAULT_PUBLIC_YN = "N";

    public static Playlist toEntity(PlaylistRequestDto request) {
        return toEntity(request, null, null);
    }

    public static Playlist toEntity(PlaylistRequestDto request, Long userId) {
        return toEntity(request, userId, null);
    }

    public static Playlist toEntity(PlaylistRequestDto request, Long userId, String coverImageUrl) {
        if (request == null) {
            return null;
        }

        Playlist playlist = new Playlist();
        playlist.setUserId(userId);
        playlist.setTitle(request.getTitle());
        playlist.setDescription(request.getDescription());
        playlist.setCoverImageUrl(coverImageUrl);
        playlist.setPublicYn(request.getPublicYn());
        playlist.setTags(request.getTags());
        return playlist;
    }

    public static Playlist toCopiedEntity(Playlist original, Long userId) {
        Playlist copied = new Playlist();
        copied.setUserId(userId);
        copied.setTitle(original.getTitle());
        copied.setDescription(original.getDescription());
        copied.setCoverImageUrl(original.getCoverImageUrl());
        copied.setPublicYn(DEFAULT_PUBLIC_YN);
        copied.setTags(original.getTags());
        copied.setTrackCount(original.getTrackCount());
        return copied;
    }

    public static PlaylistDetailResponseDto toDetailResponseDto(Playlist p,
                                                                List<PlaylistTrackResponseDto> trackResponseDtoList,
                                                                List<CommentResponseDto> commentResponseDtoList,
                                                                boolean liked) {
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
                .tracks(trackResponseDtoList)
                .comments(commentResponseDtoList)
                .tags(p.getTags())
                .liked(liked)
                .trackCount(p.getTrackCount())
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
                .tags(playlist.getTags())
                .trackCount(playlist.getTrackCount())
                .build();
    }
    }
