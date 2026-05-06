package com.example.tune_share_hub_backend.service.playlist;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.example.tune_share_hub_backend.dao.PlaylistMapperDao;
import com.example.tune_share_hub_backend.dto.PlaylistRequestDto;
import com.example.tune_share_hub_backend.dto.PlaylistResponseDto;
import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class PlaylistService {

    private final PlaylistMapperDao playlistMapper;

    // MVP-4: 생성
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

    // MVP-5: 내 목록 조회
    public List<PlaylistResponseDto> getMyPlaylists(Long userId) {
        return playlistMapper.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // MVP-6: 상세 조회
    public PlaylistResponseDto getPlaylist(Long playlistId, Long loginUserId) {
        Playlist playlist = playlistMapper.findById(playlistId);

        if (playlist == null) {
            throw new CustomException("PLAYLIST_NOT_FOUND", "플레이리스트를 찾을 수 없습니다.");
        }

        if ("N".equals(playlist.getPublicYn())) {
            if (loginUserId == null || !loginUserId.equals(playlist.getUserId())) {
                throw new CustomException("FORBIDDEN", "접근 권한이 없습니다.");
            }
        }

        return toResponse(playlist);
    }

    private PlaylistResponseDto toResponse(Playlist p) {
        return new PlaylistResponseDto(
                p.getPlaylistId(), p.getTitle(), p.getDescription(),
                p.getPublicYn(), p.getViewCount(), p.getLikeCount(),
                p.getCommentCount(), p.getCreatedAt(), Collections.emptyList()
        );
    }
}