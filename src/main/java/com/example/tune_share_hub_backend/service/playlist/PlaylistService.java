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
import com.example.tune_share_hub_backend.entity.like.Like;
import com.example.tune_share_hub_backend.entity.user.User;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import com.example.tune_share_hub_backend.service.file.FileStorageService;
import com.example.tune_share_hub_backend.validate.PlaylistValidator;
import com.example.tune_share_hub_backend.validate.UserValidator;
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
        PlaylistValidator.validatePlaylistId(playlistId);
        PlaylistValidator.validatePlaylist(playlist);

        if (hasFile(coverImage)) {
            playlist.setCoverImageUrl(fileStorageService.saveFile(coverImage));
        }

        int updatedCount = playlistDao.updatePlaylist(playlistId, userId, playlist);
        PlaylistValidator.validateUpdateCount(updatedCount, ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);

        return getPlaylist(playlistId, userId);
    }

    @Transactional
    public PlaylistDetailResponseDto updatePlaylistVisibility(Long playlistId, Long userId, Playlist playlist) {
        PlaylistValidator.validatePlaylistId(playlistId);
        PlaylistValidator.validateVisibilityRequest(playlist);

        int updatedCount = playlistDao.updatePlaylistVisibility(playlistId, userId, playlist.getPublicYn());
        PlaylistValidator.validateUpdateCount(updatedCount, ErrorCode.PLAYLIST_VISIBILITY_UPDATE_FORBIDDEN);

        return getPlaylist(playlistId, userId);
    }

    @Transactional
    public PlaylistDetailResponseDto create(Long userId, Playlist playlist, MultipartFile coverImage) {
        if (playlist != null && playlist.getPublicYn() == null) {
            playlist.setPublicYn("Y");
        }
        PlaylistValidator.validatePlaylist(playlist);

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
        PlaylistValidator.validatePlaylistId(playlistId);

        Playlist playlist = playlistDao.findById(playlistId);
        PlaylistValidator.validatePlaylistExists(playlist);
        PlaylistValidator.validatePlaylistOwner(playlist, userId, ErrorCode.PLAYLIST_DELETE_FORBIDDEN);

        playlistTrackDao.deleteByPlaylistId(playlistId);
        commentDao.deleteByPlaylistId(playlistId);
        likeDao.deleteByPlaylistId(playlistId);
        int deletedCount = playlistDao.deletePlaylist(playlistId, userId);
        PlaylistValidator.validateUpdateCount(deletedCount, ErrorCode.PLAYLIST_DELETE_FORBIDDEN);
    }

    @Transactional
    public PlaylistDetailResponseDto copyPlaylist(Long playlistId, Long userId) {
        PlaylistValidator.validatePlaylistId(playlistId);

        Playlist original = playlistDao.findById(playlistId);
        PlaylistValidator.validatePlaylistExists(original);
        PlaylistValidator.validatePublicPlaylist(original);

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
        PlaylistValidator.validatePlaylistExists(playlist);
        PlaylistValidator.validatePlaylistReadable(playlist, loginUserId);

        playlistDao.increaseViewCount(playlistId);

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

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    @Transactional
    public PlaylistDetailResponseDto addTrackToPlaylist(Long id, Long currentUserId, List<PlaylistTrack> playlistTracksList) {
        Playlist playlist = playlistDao.findById(id);
        PlaylistValidator.validatePlaylistExists(playlist);
        PlaylistValidator.validatePlaylistOwner(playlist, currentUserId, ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);
        PlaylistValidator.validateRequestList(playlistTracksList);

        List<PlaylistTrack> playlistTracks = playlistTrackDao.findByPlaylistId(id);

        int nextPositionNo = getNextPositionNo(playlistTracks);

        for (PlaylistTrack playlistTrack : playlistTracksList) {
            playlistTrack.setPlaylistId(id);
            playlistTrack.setPositionNo(nextPositionNo);
            nextPositionNo++;
        }

        playlistTrackDao.insertPlaylistTracks(playlistTracksList);

        playlistDao.increaseTrackCount(id, playlistTracksList.size());


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
        PlaylistValidator.validateTrackId(trackId);

        Playlist playlist = playlistDao.findById(id);
        PlaylistValidator.validatePlaylistExists(playlist);
        PlaylistValidator.validatePlaylistOwner(playlist, currentUserId, ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);

        PlaylistValidator.validateTrackExists(playlistTrackDao.existsByPlaylistIdAndTrackId(id, trackId));

        int deletedCount = playlistTrackDao.deletePlaylistTrack(trackId);
        PlaylistValidator.validateTrackDeleteCount(deletedCount);

        playlistDao.decreaseTrackCount(id);

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
        PlaylistValidator.validatePlaylistExists(playlist);
        PlaylistValidator.validatePlaylistOwner(playlist, currentUserId, ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);

        List<PlaylistTrack> existingTracks = playlistTrackDao.findByPlaylistId(id);
        PlaylistValidator.validateReorderTracks(existingTracks, requestTracks);

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
        PlaylistValidator.validateCommentRequest(comment);

        Playlist playlist = playlistDao.findById(id);
        PlaylistValidator.validatePlaylistExists(playlist);
        PlaylistValidator.validateCommentWritable(playlist, userId);

        User user = userDao.getUserById(userId);
        UserValidator.validateUserExists(user);

        comment.setPlaylistId(id);
        comment.setUserId(userId);
        comment.setUserNickname(user.getNickname());

        commentDao.insertComment(comment);
        playlistDao.increaseCommentCount(id);

        return getPlaylist(id, userId);
    }

    @Transactional
    public PlaylistDetailResponseDto updateComment(Long id, Long commentId, Long userId, Comment comment) {
        PlaylistValidator.validateCommentRequest(comment);

        Comment existingComment = commentDao.findById(commentId);
        PlaylistValidator.validateCommentExists(existingComment, id);
        PlaylistValidator.validateCommentOwner(existingComment, userId);

        existingComment.setContent(comment.getContent());
        commentDao.updateComment(existingComment);

        return getPlaylist(id, userId);
    }

    @Transactional
    public PlaylistDetailResponseDto deleteComment(Long id, Long commentId, Long userId) {
        Comment existingComment = commentDao.findById(commentId);
        PlaylistValidator.validateCommentExists(existingComment, id);
        PlaylistValidator.validateCommentOwner(existingComment, userId);

        commentDao.deleteComment(commentId);
        playlistDao.decreaseCommentCount(id);

        return getPlaylist(id, userId);
    }

    public List<PlaylistResponseDto> getPlaylistRanking(int limit, String type) {
        PlaylistValidator.validateRankingType(type);
        return playlistDao.findTopPlaylists(limit, type)
                .stream()
                .map(PlaylistConvert::toResponseDto)
                .collect(Collectors.toList());
    }
}
