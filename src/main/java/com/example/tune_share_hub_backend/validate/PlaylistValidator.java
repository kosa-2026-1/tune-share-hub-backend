package com.example.tune_share_hub_backend.validate;

import com.example.tune_share_hub_backend.entity.Comment;
import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.entity.PlaylistTrack;
import com.example.tune_share_hub_backend.dto.playlist.CommentRequestDto;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlaylistValidator {

    public static void validatePlaylistId(Long playlistId) {
        if (playlistId == null || playlistId <= 0) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
        }
    }

    public static void validatePlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (playlist.getTitle() == null || playlist.getTitle().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_TITLE);
        }

        validatePublicYn(playlist.getPublicYn());
    }

    public static void validateVisibilityRequest(Playlist playlist) {
        if (playlist == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        validatePublicYn(playlist.getPublicYn());
    }

    public static void validatePublicYn(String publicYn) {
        if (!"Y".equals(publicYn) && !"N".equals(publicYn)) {
            throw new CustomException(ErrorCode.INVALID_PUBLIC_YN);
        }
    }

    public static void validatePlaylistExists(Playlist playlist) {
        if (playlist == null) {
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        }
    }

    public static void validatePlaylistOwner(Playlist playlist, Long userId, ErrorCode errorCode) {
        if (!playlist.getUserId().equals(userId)) {
            throw new CustomException(errorCode);
        }
    }

    public static void validatePublicPlaylist(Playlist playlist) {
        if (!"Y".equals(playlist.getPublicYn())) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
        }
    }

    public static void validatePlaylistReadable(Playlist playlist, Long loginUserId) {
        if ("N".equals(playlist.getPublicYn())
                && (loginUserId == null || !loginUserId.equals(playlist.getUserId()))) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
        }
    }

    public static void validateUpdateCount(int updatedCount, ErrorCode errorCode) {
        if (updatedCount == 0) {
            throw new CustomException(errorCode);
        }
    }

    public static void validateRequestList(List<?> requestList) {
        if (requestList == null || requestList.isEmpty() || requestList.contains(null)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    public static void validateTrackId(Long trackId) {
        if (trackId == null || trackId <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    public static void validateTrackExists(int existsCount) {
        if (existsCount == 0) {
            throw new CustomException(ErrorCode.PLAYLIST_TRACK_NOT_FOUND);
        }
    }

    public static void validateTrackDeleteCount(int deletedCount) {
        if (deletedCount == 0) {
            throw new CustomException(ErrorCode.PLAYLIST_TRACK_NOT_FOUND);
        }
    }

    public static void validateReorderTrackList(List<PlaylistTrack> existingTrackList, List<PlaylistTrack> requestTrackList) {
        validateRequestList(requestTrackList);

        if (existingTrackList.size() != requestTrackList.size()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Set<Long> uniqueTrackIds = requestTrackList.stream()
                .map(PlaylistTrack::getPlaylistTrackId)
                .collect(Collectors.toSet());
        if (uniqueTrackIds.size() != requestTrackList.size()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Set<Long> existingTrackIds = existingTrackList.stream()
                .map(PlaylistTrack::getPlaylistTrackId)
                .collect(Collectors.toSet());

        for (PlaylistTrack requestTrack : requestTrackList) {
            if (requestTrack.getPlaylistTrackId() == null) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }

            if (!existingTrackIds.contains(requestTrack.getPlaylistTrackId())) {
                throw new CustomException(ErrorCode.PLAYLIST_TRACK_NOT_FOUND);
            }
        }
    }

    public static void validateCommentRequest(Comment comment) {
        if (comment == null || comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    public static void validateCommentRequestDto(CommentRequestDto requestDto) {
        if (requestDto == null || requestDto.getContent() == null || requestDto.getContent().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    public static void validateCommentExists(Comment comment, Long playlistId) {
        if (comment == null || !comment.getPlaylistId().equals(playlistId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
    }

    public static void validateCommentOwner(Comment comment, Long userId) {
        if (!comment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
        }
    }

    public static void validateCommentWritable(Playlist playlist, Long userId) {
        if ("N".equals(playlist.getPublicYn()) && !playlist.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        }
    }

    public static void validateRankingType(String type) {
        if (!"like".equals(type) && !"view".equals(type)) {
            throw new CustomException(ErrorCode.INVALID_RANKING_TYPE);
        }
    }
}
