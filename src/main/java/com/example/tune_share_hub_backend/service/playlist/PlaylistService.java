package com.example.tune_share_hub_backend.service.playlist;

import com.example.tune_share_hub_backend.dao.like.LikeDao;
import com.example.tune_share_hub_backend.dto.like.LikeResponseDto;
import com.example.tune_share_hub_backend.dao.playlist.CommentDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistMapperDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistTrackDao;
import com.example.tune_share_hub_backend.dao.user.UserDao;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackReorderRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.entity.PlaylistTrack;
import com.example.tune_share_hub_backend.entity.like.Like;
import com.example.tune_share_hub_backend.entity.Comment;
import com.example.tune_share_hub_backend.entity.user.User;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistMapperDao playlistMapper;
    private final PlaylistTrackDao playlistTrackDao;
    private final LikeDao likeDao;
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
        playlist.setPublicYn(req.getPublicYn() == null ? "Y" : req.getPublicYn());

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
        result.put("totalPages", (int)Math.ceil((double)total / size));
        return result;
    }

    @Transactional
    public void deletePlaylist(Long playlistId, Long userId) {
        validatePlaylistId(playlistId);

        int deletedCount = playlistMapper.deletePlaylist(playlistId, userId);

        if (deletedCount == 0) {
            throw new CustomException(ErrorCode.PLAYLIST_DELETE_FORBIDDEN);
        }
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

    ;

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
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
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

    public void updateComment(Long id, Long commentId, Long userId, Comment comment) {
        if (comment == null || comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Comment existingComment = commentDao.findById(commentId);
        if (existingComment == null || !existingComment.getPlaylistId().equals(id)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        if (!existingComment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
        }

        existingComment.setContent(comment.getContent());
        commentDao.updateComment(existingComment);
    }

    @Transactional
    public void deleteComment(Long id, Long commentId, Long userId) {
        Comment existingComment = commentDao.findById(commentId);
        if (existingComment == null || !existingComment.getPlaylistId().equals(id)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        if (!existingComment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
        }

        commentDao.deleteComment(commentId);
        playlistMapper.decreaseCommentCount(id);
    }

    @Transactional
    public LikeResponseDto like(Long playlistId, Long userId) {
        // 플레이리스트 유효성 검사
        Playlist playlist = playlistMapper.findById(playlistId);
        if (playlist == null) throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        if ("N".equals(playlist.getPublicYn())) throw new CustomException(ErrorCode.PRIVATE_PLAYLIST_CANNOT_BE_LIKED);

        // 상태 조회 및 결정
        Like existingLike = likeDao.getLikeByUserIdAndPlaylistId(playlistId, userId);

        // 이력이 없거나 현재 상태가 'N'이면 -> 좋아요('Y') / 아니면 -> 취소('N')
        boolean isActionLike = (existingLike == null || "N".equals(existingLike.getStatus()));
        String newStatus = isActionLike ? "Y" : "N";

        // DB 반영 (Insert or Update)
        if (existingLike == null) {
            likeDao.insertLike(playlistId, userId, newStatus);
        } else {
            likeDao.updateLikeStatus(playlistId, userId, newStatus);
        }

        // 플레이리스트 카운트 업데이트
        if (isActionLike) {
            likeDao.incrementLikeCount(playlistId);
        } else {
            likeDao.decrementLikeCount(playlistId);
        }

        Playlist updatedPlaylist = playlistMapper.findById(playlistId);

        return LikeResponseDto.builder()
                .playlistId(playlistId)
                .userId(userId)
                .status(newStatus)
                .totalLikeCount(updatedPlaylist.getLikeCount())
                .build();
    }

    @Transactional
    public List<PlaylistResponseDto> getLikedPlaylists(Long userId){
        // 사용자 유효성 검사
        User user = userDao.getUserById(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return likeDao.getLikedPlaylistsByUserId(userId)
                .stream()
                .map(PlaylistResponseDto::from).toList();
    }
}
