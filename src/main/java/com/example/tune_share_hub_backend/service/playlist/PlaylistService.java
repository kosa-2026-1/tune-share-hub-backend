package com.example.tune_share_hub_backend.service.playlist;

import com.example.tune_share_hub_backend.dao.playlist.PlaylistMapperDao;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistRequestDto;
import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistMapperDao playlistMapper;

    @Transactional
    public void updatePlaylist(Long playlistId, Long userId, PlaylistRequestDto request) {
        validatePlaylistId(playlistId);
        validateRequest(request);

        int updatedCount = playlistMapper.updatePlaylist(playlistId, userId, request);
        if (updatedCount == 0) {
            throw new CustomException(ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);
        }
    }

    @Transactional
    public void updatePlaylistVisibility(Long playlistId, Long userId, PlaylistRequestDto request) {
        validatePlaylistId(playlistId);
        validateVisibilityRequest(request);

        int updatedCount = playlistMapper.updatePlaylistVisibility(playlistId, userId, request.getPublicYn());
        if (updatedCount == 0) {
            throw new CustomException(ErrorCode.PLAYLIST_VISIBILITY_UPDATE_FORBIDDEN);
        }
    }

    public PlaylistResponseDto create(Long userId, PlaylistRequestDto req) {
        Playlist playlist = new Playlist();
        playlist.setUserId(userId);
        playlist.setTitle(req.getTitle());
        playlist.setDescription(req.getDescription());
        playlist.setCoverImageUrl(req.getCoverImageUrl());
        playlist.setPublicYn(req.getPublicYn());

        playlistMapper.insert(playlist);

        return toResponse(playlist);
    }

    public Map<String, Object> getPublicPlaylists(int page, int size) {
    int offset = (page - 1) * size;
    List<PlaylistResponseDto> list = playlistMapper.findPublicPlaylists(offset, size)
            .stream().map(this::toResponse).collect(Collectors.toList());
    int total = playlistMapper.countPublicPlaylists();

    Map<String, Object> result = new HashMap<>();
    result.put("content", list);
    result.put("totalCount", total);
    result.put("currentPage", page);
    result.put("totalPages", (int) Math.ceil((double) total / size));
    return result;
}

    public List<PlaylistResponseDto> getMyPlaylists(Long userId) {
        return playlistMapper.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PlaylistResponseDto getPlaylist(Long playlistId, Long loginUserId) {
        Playlist playlist = playlistMapper.findById(playlistId);

        if (playlist == null) {
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        }

        if ("N".equals(playlist.getPublicYn())) {
            if (loginUserId == null || !loginUserId.equals(playlist.getUserId())) {
                throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
            }
        }

        return toResponse(playlist);
    }

    private void validatePlaylistId(Long playlistId) {
        if (playlistId == null || playlistId <= 0) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
        }
    }

    private void validateRequest(PlaylistRequestDto request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_TITLE);
        }

        validatePublicYn(request.getPublicYn());
    }

    private void validateVisibilityRequest(PlaylistRequestDto request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        validatePublicYn(request.getPublicYn());
    }

    private void validatePublicYn(String publicYn) {
        if (!"Y".equals(publicYn) && !"N".equals(publicYn)) {
            throw new CustomException(ErrorCode.INVALID_PUBLIC_YN);
        }
    }

    private PlaylistResponseDto toResponse(Playlist p) {
    return new PlaylistResponseDto(
            p.getPlaylistId(), p.getTitle(), p.getDescription(),
            p.getPublicYn(), p.getViewCount(), p.getLikeCount(),
            p.getCoverImageUrl(), p.getCommentCount(), p.getCreatedAt(), 
            Collections.emptyList());
}
}
