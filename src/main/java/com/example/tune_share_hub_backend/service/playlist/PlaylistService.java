package com.example.tune_share_hub_backend.service.playlist;

import com.example.tune_share_hub_backend.convert.CommentConvert;
import com.example.tune_share_hub_backend.convert.PlaylistConvert;
import com.example.tune_share_hub_backend.convert.PlaylistTrackConvert;
import com.example.tune_share_hub_backend.dao.like.LikeDao;
import com.example.tune_share_hub_backend.dao.playlist.CommentDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistTrackDao;
import com.example.tune_share_hub_backend.dao.user.UserDao;
import com.example.tune_share_hub_backend.dto.music.CommentResponseDto;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistDetailResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.entity.Comment;
import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.entity.PlaylistTrack;
import com.example.tune_share_hub_backend.entity.user.User;
import com.example.tune_share_hub_backend.entity.like.Like;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import com.example.tune_share_hub_backend.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final FileStorageService fileStorageService;
    private final PlaylistDao playlistDao;
    private final PlaylistTrackDao playlistTrackDao;
    private final CommentDao commentDao;
    private final UserDao userDao;
    private final LikeDao likeDao;

    @Transactional
    public PlaylistDetailResponseDto updatePlaylist(Long playlistId, Long userId, Playlist playlist, MultipartFile coverImage) {
        validatePlaylistId(playlistId);
        validatePlaylist(playlist);

        if (hasFile(coverImage)) {
            playlist.setCoverImageUrl(fileStorageService.saveFile(coverImage));
        }

        int updatedCount = playlistDao.updatePlaylist(playlistId, userId, playlist);
        if (updatedCount == 0) {
            throw new CustomException(ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);
        }

        return getPlaylist(playlistId, userId);
    }

    @Transactional
    public PlaylistDetailResponseDto updatePlaylistVisibility(Long playlistId, Long userId, Playlist playlist) {
        validatePlaylistId(playlistId);
        validateVisibilityRequest(playlist);

        int updatedCount = playlistDao.updatePlaylistVisibility(playlistId, userId, playlist.getPublicYn());
        if (updatedCount == 0) {
            throw new CustomException(ErrorCode.PLAYLIST_VISIBILITY_UPDATE_FORBIDDEN);
        }

        return getPlaylist(playlistId, userId);
    }

    @Transactional
    public PlaylistDetailResponseDto create(Long userId, Playlist playlist, MultipartFile coverImage) {
        if (playlist != null && playlist.getPublicYn() == null) {
            playlist.setPublicYn("Y");
        }
        validatePlaylist(playlist);

        playlist.setUserId(userId);
        if (hasFile(coverImage)) {
            playlist.setCoverImageUrl(fileStorageService.saveFile(coverImage));
        }

        playlistDao.insert(playlist);

        return getPlaylist(playlist.getPlaylistId(), userId);
    }

    public Map<String, Object> getPublicPlaylists(int page, int size) {
        int offset = (page - 1) * size;
        List<PlaylistResponseDto> list = playlistDao.findPublicPlaylists(offset, size)
                .stream()
                .map(PlaylistConvert::toResponseDto)
                .collect(Collectors.toList());
        int total = playlistDao.countPublicPlaylists();

        Map<String, Object> result = new HashMap<>();
        result.put("content", list);
        result.put("totalCount", total);
        result.put("currentPage", page);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    @Transactional
    public void deletePlaylist(Long playlistId, Long userId) {
        validatePlaylistId(playlistId);

        Playlist playlist = playlistDao.findById(playlistId);
        if (playlist == null) {
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        }
        if (!playlist.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.PLAYLIST_DELETE_FORBIDDEN);
        }

        playlistTrackDao.deleteByPlaylistId(playlistId);
        commentDao.deleteByPlaylistId(playlistId);
        likeDao.deleteByPlaylistId(playlistId);
        int deletedCount = playlistDao.deletePlaylist(playlistId, userId);

        if (deletedCount == 0) {
            throw new CustomException(ErrorCode.PLAYLIST_DELETE_FORBIDDEN);
        }
    }

    @Transactional
    public PlaylistDetailResponseDto copyPlaylist(Long playlistId, Long userId) {
        validatePlaylistId(playlistId);

        Playlist original = playlistDao.findById(playlistId);
        if (original == null) {
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        }

        if (!"Y".equals(original.getPublicYn())) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
        }

        Playlist copied = PlaylistConvert.toCopiedEntity(original, userId);

        playlistDao.insert(copied);

        List<PlaylistTrack> tracks = playlistTrackDao.findByPlaylistId(playlistId);
        for (PlaylistTrack track : tracks) {
            track.setPlaylistId(copied.getPlaylistId());
        }
        if (!tracks.isEmpty()) {
            playlistTrackDao.insertPlaylistTracks(tracks);
        }

        return getPlaylist(copied.getPlaylistId(), userId);
    }

    public List<PlaylistResponseDto> getMyPlaylists(Long userId) {
        return playlistDao.findByUserId(userId)
                .stream()
                .map(PlaylistConvert::toResponseDto)
                .collect(Collectors.toList());
    }

    public PlaylistDetailResponseDto getPlaylist(Long playlistId, Long loginUserId) {
        Playlist playlist = playlistDao.findById(playlistId);

        if (playlist == null) {
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        }

        if ("N".equals(playlist.getPublicYn())) {
            if (loginUserId == null || !loginUserId.equals(playlist.getUserId())) {
                throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
            }
        }

        List<PlaylistTrackResponseDto> tracks = PlaylistTrackConvert.toResponseDtoList(
                playlistTrackDao.findByPlaylistId(playlistId));
        List<CommentResponseDto> comments = CommentConvert.toCommentResponseDtoList(
                commentDao.findByPlaylistId(playlistId));

        boolean likeStatus = false;
        if (loginUserId != null) {
            Like like = likeDao.getLikeByUserIdAndPlaylistId(loginUserId, playlistId);
            likeStatus = (like != null && "Y".equals(like.getStatus()));
        }

        return PlaylistConvert.toDetailResponseDto(playlist, tracks, comments, likeStatus);
    }

    private void validatePlaylistId(Long playlistId) {
        if (playlistId == null || playlistId <= 0) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
        }
    }

    private void validatePlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (playlist.getTitle() == null || playlist.getTitle().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_TITLE);
        }

        validatePublicYn(playlist.getPublicYn());
    }

    private void validateVisibilityRequest(Playlist playlist) {
        if (playlist == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        validatePublicYn(playlist.getPublicYn());
    }

    private void validatePublicYn(String publicYn) {
        if (!"Y".equals(publicYn) && !"N".equals(publicYn)) {
            throw new CustomException(ErrorCode.INVALID_PUBLIC_YN);
        }
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    @Transactional
    public PlaylistDetailResponseDto addTrackToPlaylist(Long id, Long currentUserId, List<PlaylistTrack> playlistTracksList) {
        Playlist playlist = playlistDao.findById(id);

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

        return getPlaylist(id, currentUserId);
    }

    private int getNextPositionNo(List<PlaylistTrack> playlistTracks) {
        if (playlistTracks == null || playlistTracks.isEmpty()) {
            return 1;
        }

        return playlistTracks.stream()
                .map(PlaylistTrack::getPositionNo)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    @Transactional
    public PlaylistDetailResponseDto removeTrackFromPlaylist(Long id, Long currentUserId, Long trackId) {
        if (trackId == null || trackId <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Playlist playlist = playlistDao.findById(id);

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

        return getPlaylist(id, currentUserId);
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
    public PlaylistDetailResponseDto reorderTrack(Long id, Long currentUserId, List<PlaylistTrack> requestTracks) {
        Playlist playlist = playlistDao.findById(id);

        if (playlist == null) {
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        }
        if (!playlist.getUserId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);
        }

        if (requestTracks == null || requestTracks.isEmpty() || requestTracks.contains(null)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        List<PlaylistTrack> existingTracks = playlistTrackDao.findByPlaylistId(id);
        if (existingTracks.size() != requestTracks.size()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Set<Long> uniqueTrackIds = requestTracks.stream()
                .map(PlaylistTrack::getPlaylistTrackId)
                .collect(Collectors.toSet());
        if (uniqueTrackIds.size() != requestTracks.size()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Set<Long> existingTrackIds = existingTracks.stream()
                .map(PlaylistTrack::getPlaylistTrackId)
                .collect(Collectors.toSet());

        for (PlaylistTrack requestTrack : requestTracks) {
            if (requestTrack.getPlaylistTrackId() == null) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }

            if (!existingTrackIds.contains(requestTrack.getPlaylistTrackId())) {
                throw new CustomException(ErrorCode.PLAYLIST_TRACK_NOT_FOUND);
            }
        }

        int maxPositionNo = existingTracks.stream()
                .map(PlaylistTrack::getPositionNo)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        int temporaryPositionStart = maxPositionNo + requestTracks.size() + 1;

        List<PlaylistTrack> temporaryPositions = new ArrayList<>();
        for (int i = 0; i < requestTracks.size(); i++) {
            temporaryPositions.add(PlaylistTrackConvert.toPositionEntity(
                    requestTracks.get(i).getPlaylistTrackId(), temporaryPositionStart + i));
        }
        playlistTrackDao.updatePlaylistTrackPositions(temporaryPositions);

        List<PlaylistTrack> finalPositions = new ArrayList<>();
        for (int i = 0; i < requestTracks.size(); i++) {
            finalPositions.add(PlaylistTrackConvert.toPositionEntity(
                    requestTracks.get(i).getPlaylistTrackId(), i + 1));
        }
        playlistTrackDao.updatePlaylistTrackPositions(finalPositions);

        return getPlaylist(id, currentUserId);
    }

    @Transactional
    public PlaylistDetailResponseDto createComment(Long id, Long userId, Comment comment) {
        if (comment == null || comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Playlist playlist = playlistDao.findById(id);
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
        playlistDao.increaseCommentCount(id);

        return getPlaylist(id, userId);
    }

    @Transactional
    public PlaylistDetailResponseDto updateComment(Long id, Long commentId, Long userId, Comment comment) {
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

        return getPlaylist(id, userId);
    }

    public void increaseViewCount(Long playlistId) {
        playlistDao.increaseViewCount(playlistId);
    }

    public void increaseTrackCount(Long playlistId) {
        playlistDao.increaseTrackCount(playlistId);
    }

    public void decreaseTrackCount(Long playlistId) {
        playlistDao.decreaseTrackCount(playlistId);
    }

    @Transactional
    public PlaylistDetailResponseDto deleteComment(Long id, Long commentId, Long userId) {
        Comment existingComment = commentDao.findById(commentId);
        if (existingComment == null || !existingComment.getPlaylistId().equals(id)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        if (!existingComment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.INVALID_PLAYLIST_ID);
        }

        commentDao.deleteComment(commentId);
        playlistDao.decreaseCommentCount(id);

        return getPlaylist(id, userId);
    }

    public List<PlaylistResponseDto> getPlaylistRanking(int limit, String type) {
        if (!"like".equals(type) && !"view".equals(type)) {
            throw new CustomException(ErrorCode.INVALID_RANKING_TYPE);
        }
        return playlistDao.findTopPlaylists(limit, type)
                .stream()
                .map(PlaylistConvert::toResponseDto)
                .collect(Collectors.toList());
    }
}
