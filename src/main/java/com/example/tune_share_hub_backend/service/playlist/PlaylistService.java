package com.example.tune_share_hub_backend.service.playlist;

import com.example.tune_share_hub_backend.dao.playlist.CommentDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistMapperDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistTrackDao;
import com.example.tune_share_hub_backend.dao.user.UserDao;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackReorderRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistRequestDto;
import com.example.tune_share_hub_backend.entity.Comment;
import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.entity.PlaylistTrack;
import com.example.tune_share_hub_backend.entity.user.User;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistMapperDao playlistMapper;
    private final PlaylistTrackDao playlistTrackDao;
    private final CommentDao commentDao;
    private final UserDao userDao;

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
                p.getCommentCount(), p.getCreatedAt(), Collections.emptyList()
        );
    }

    @Transactional
    public List<PlaylistTrack> addTrackToPlaylist(
        Long id,
        Long currentUserId,
        List<PlaylistTrack> playlistTracksList
    ) {
        Playlist playlist = playlistMapper.findById(id);

        if (playlist == null) {
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        }

        if (!playlist.getUserId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);
        }

        if (playlistTracksList == null || playlistTracksList.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        List<PlaylistTrack> playlistTracks = playlistTrackDao.findByPlaylistId(id);

        int nextPositionNo = getNextPositionNo(playlistTracks);

        for (PlaylistTrack playlistTrack : playlistTracksList) {
            playlistTrack.setPlaylistId(id);
            playlistTrack.setPositionNo(nextPositionNo);
            nextPositionNo++;
        }

        playlistTrackDao.insertPlaylistTracks(playlistTracksList);

        return playlistTrackDao.findByPlaylistId(id);
    }

    private int getNextPositionNo(List<PlaylistTrack> playlistTracks) {
        if (playlistTracks == null || playlistTracks.isEmpty()) {
            return 1;
        }

        return playlistTracks.stream()
            .map(PlaylistTrack::getPositionNo)
            .filter(positionNo -> positionNo != null)
            .max(Integer::compareTo)
            .orElse(0) + 1;
    }

    @Transactional
    public void removeTrackFromPlaylist(Long id, Long currentUserId, Long trackId) {
            if (trackId == null || trackId <= 0) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }

            Playlist playlist = playlistMapper.findById(id);

            if (playlist == null) {
                throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
            }
            if (!playlist.getUserId().equals(currentUserId)) {
                throw new CustomException(ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);
            }

            if (playlistTrackDao.existsByPlaylistIdAndTrackId(id, trackId) == 0) {
                throw new CustomException(ErrorCode.PLAYLIST_TRACK_NOT_FOUND);
            }

            int deletedCount = playlistTrackDao.deletePlaylistTrack(trackId);
            if (deletedCount == 0) {
                throw new CustomException(ErrorCode.PLAYLIST_TRACK_NOT_FOUND);
            }

            compactPlaylistTrackPositions(id);
    }

    private void compactPlaylistTrackPositions(Long playlistId) {
        List<PlaylistTrack> playlistTracks = playlistTrackDao.findByPlaylistId(playlistId);
        List<PlaylistTrack> tracksToUpdate = new ArrayList<>();

        for (int i = 0; i < playlistTracks.size(); i++) {
            PlaylistTrack playlistTrack = playlistTracks.get(i);
            int positionNo = i + 1;

            if (playlistTrack.getPositionNo() == null || playlistTrack.getPositionNo() != positionNo) {
                playlistTrack.setPositionNo(positionNo);
                tracksToUpdate.add(playlistTrack);
            }
        }

        if (!tracksToUpdate.isEmpty()) {
            playlistTrackDao.updatePlaylistTrackPositions(tracksToUpdate);
        }
    }

    @Transactional
    public void reorderTrack(Long id, Long currentUserId, List<PlaylistTrackReorderRequestDto> requestListDto) {
        Playlist playlist = playlistMapper.findById(id);

        if (playlist == null) {
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        }
        if (!playlist.getUserId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);
        }

        if (requestListDto == null || requestListDto.isEmpty() || requestListDto.contains(null)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        List<PlaylistTrack> existingTracks = playlistTrackDao.findByPlaylistId(id);
        if (existingTracks.size() != requestListDto.size()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Set<Long> uniqueTrackIds = requestListDto.stream()
            .map(PlaylistTrackReorderRequestDto::getPlaylistTrackId)
            .collect(Collectors.toSet());
        if (uniqueTrackIds.size() != requestListDto.size()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Set<Long> existingTrackIds = existingTracks.stream()
            .map(PlaylistTrack::getPlaylistTrackId)
            .collect(Collectors.toSet());

        for (PlaylistTrackReorderRequestDto requestDto : requestListDto) {
            if (requestDto.getPlaylistTrackId() == null) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }

            if (!existingTrackIds.contains(requestDto.getPlaylistTrackId())) {
                throw new CustomException(ErrorCode.PLAYLIST_TRACK_NOT_FOUND);
            }
        }

        int maxPositionNo = existingTracks.stream()
            .map(PlaylistTrack::getPositionNo)
            .filter(positionNo -> positionNo != null)
            .max(Integer::compareTo)
            .orElse(0);
        int temporaryPositionStart = maxPositionNo + requestListDto.size() + 1;

        List<PlaylistTrack> temporaryPositions = new ArrayList<>();
        for (int i = 0; i < requestListDto.size(); i++) {
            temporaryPositions.add(PlaylistTrack.builder()
                .playlistTrackId(requestListDto.get(i).getPlaylistTrackId())
                .positionNo(temporaryPositionStart + i)
                .build());
        }
        playlistTrackDao.updatePlaylistTrackPositions(temporaryPositions);

        List<PlaylistTrack> finalPositions = new ArrayList<>();
        for (int i = 0; i < requestListDto.size(); i++) {
            finalPositions.add(PlaylistTrack.builder()
                .playlistTrackId(requestListDto.get(i).getPlaylistTrackId())
                .positionNo(i + 1)
                .build());
        }
        playlistTrackDao.updatePlaylistTrackPositions(finalPositions);
    }

    @Transactional
    public void createComment(Long id, Long userId, Comment comment) {
        if (comment == null || comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Playlist playlist = playlistMapper.findById(id);
        if (playlist == null) {
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        }

        if ("N".equals(playlist.getPublicYn()) && !playlist.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
        }

        User user = userDao.getUserById(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        comment.setPlaylistId(id);
        comment.setUserId(userId);
        comment.setUserNickname(user.getNickname());

        commentDao.insertComment(comment);
        playlistMapper.increaseCommentCount(id);

    }
}
