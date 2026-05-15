package com.example.tune_share_hub_backend.service.playlist;

import com.example.tune_share_hub_backend.convert.CommentConvert;
import com.example.tune_share_hub_backend.convert.PlaylistConvert;
import com.example.tune_share_hub_backend.convert.PlaylistTrackConvert;
import com.example.tune_share_hub_backend.dao.like.LikeDao;
import com.example.tune_share_hub_backend.dao.playlist.CommentDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistTrackDao;
import com.example.tune_share_hub_backend.dao.user.UserDao;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.CommentResponseDto;
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
    public PlaylistDetailResponseDto createPlaylist(Long userId, Playlist playlist, MultipartFile coverImage) {
        if (playlist != null && playlist.getPublicYn() == null) {
            playlist.setPublicYn("Y");
        }
        PlaylistValidator.validatePlaylist(playlist);

        playlist.setUserId(userId);
        if (hasFile(coverImage)) {
            playlist.setCoverImageUrl(fileStorageService.saveFile(coverImage));
        }

        playlistDao.insert(playlist);

        return getPlaylistDetail(playlist.getPlaylistId(), userId);
    }

    @Transactional
    public PlaylistDetailResponseDto copyPlaylist(Long playlistId, Long userId) {
        PlaylistValidator.validatePlaylistId(playlistId);

        Playlist original = playlistDao.findById(playlistId);
        PlaylistValidator.validatePlaylistExists(original);
        PlaylistValidator.validatePublicPlaylist(original);

        List<PlaylistTrack> playlistTrackList = playlistTrackDao.findByPlaylistId(playlistId);
        Playlist copied = PlaylistConvert.toCopiedEntity(original, userId);
        copied.setTrackCount(playlistTrackList.size());
        playlistDao.insert(copied);

        if (!playlistTrackList.isEmpty()) {
            playlistTrackDao.copyPlaylistTracks(playlistId, copied.getPlaylistId());
        }

        return getPlaylistDetail(copied.getPlaylistId(), userId);
    }

    @Transactional
    public PlaylistDetailResponseDto addPlaylistTrackList(Long id, Long currentUserId, List<PlaylistTrack> requestPlaylistTrackList) {
        Playlist playlist = playlistDao.findById(id);
        PlaylistValidator.validatePlaylistExists(playlist);
        PlaylistValidator.validatePlaylistOwner(playlist, currentUserId, ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);
        PlaylistValidator.validateRequestList(requestPlaylistTrackList);

        List<PlaylistTrack> existingPlaylistTrackList = playlistTrackDao.findByPlaylistId(id);

        int nextPositionNo = getNextPositionNo(existingPlaylistTrackList);

        for (PlaylistTrack playlistTrack : requestPlaylistTrackList) {
            playlistTrack.setPlaylistId(id);
            playlistTrack.setPositionNo(nextPositionNo);
            nextPositionNo++;
        }

        playlistTrackDao.insertPlaylistTracks(requestPlaylistTrackList);

        playlistDao.increaseTrackCount(id, requestPlaylistTrackList.size());


        return getPlaylistDetail(id, currentUserId);
    }

    @Transactional
    public PlaylistDetailResponseDto createPlaylistComment(Long id, Long userId, Comment comment) {
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

        return getPlaylistDetail(id, userId);
    }

    public Map<String, Object> getPublicPlaylistList(int page, int size) {
        int offset = (page - 1) * size;
        List<PlaylistResponseDto> playlistResponseDtoList = playlistDao.findPublicPlaylistList(offset, size)
                .stream()
                .map(PlaylistConvert::toResponseDto)
                .collect(Collectors.toList());
        int total = playlistDao.countPublicPlaylistList();

        Map<String, Object> result = new HashMap<>();
        result.put("content", playlistResponseDtoList);
        result.put("totalCount", total);
        result.put("currentPage", page);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    public List<PlaylistResponseDto> getMyPlaylistList(Long userId) {
        return playlistDao.findByUserId(userId)
                .stream()
                .map(PlaylistConvert::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<PlaylistResponseDto> getPlaylistRanking(int limit, String type) {
        PlaylistValidator.validateRankingType(type);
        return playlistDao.findTopPlaylistList(limit, type)
                .stream()
                .map(PlaylistConvert::toResponseDto)
                .collect(Collectors.toList());
    }

    public PlaylistDetailResponseDto getPlaylistDetail(Long playlistId, Long loginUserId) {
        Playlist playlist = playlistDao.findById(playlistId);
        PlaylistValidator.validatePlaylistExists(playlist);
        PlaylistValidator.validatePlaylistReadable(playlist, loginUserId);

        playlistDao.increaseViewCount(playlistId);

        List<PlaylistTrackResponseDto> trackResponseDtoList = PlaylistTrackConvert.toResponseDtoList(
                playlistTrackDao.findByPlaylistId(playlistId));
        List<CommentResponseDto> commentResponseDtoList = CommentConvert.toCommentResponseDtoList(
                commentDao.findByPlaylistId(playlistId));

        boolean likeStatus = false;
        if (loginUserId != null) {
            Like like = likeDao.getLikeByUserIdAndPlaylistId(playlistId, loginUserId);
            likeStatus = (like != null && "Y".equals(like.getStatus()));
        }

        return PlaylistConvert.toDetailResponseDto(playlist, trackResponseDtoList, commentResponseDtoList, likeStatus);
    }

    @Transactional
    public PlaylistDetailResponseDto updatePlaylist(Long playlistId, Long userId, Playlist playlist, MultipartFile coverImage) {
        PlaylistValidator.validatePlaylistId(playlistId);
        PlaylistValidator.validatePlaylist(playlist);

        if (hasFile(coverImage)) {
            playlist.setCoverImageUrl(fileStorageService.saveFile(coverImage));
        }

        int updatedCount = playlistDao.updatePlaylist(playlistId, userId, playlist);
        PlaylistValidator.validateUpdateCount(updatedCount, ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);

        return getPlaylistDetail(playlistId, userId);
    }

    @Transactional
    public PlaylistDetailResponseDto updatePlaylistVisibility(Long playlistId, Long userId, String publicYn) {
        PlaylistValidator.validatePlaylistId(playlistId);

        int updatedCount = playlistDao.updatePlaylistVisibility(playlistId, userId, publicYn);
        PlaylistValidator.validateUpdateCount(updatedCount, ErrorCode.PLAYLIST_VISIBILITY_UPDATE_FORBIDDEN);

        return getPlaylistDetail(playlistId, userId);
    }

    @Transactional
    public PlaylistDetailResponseDto updatePlaylistTrackOrder(Long id, Long currentUserId, List<PlaylistTrack> requestTrackList) {
        Playlist playlist = playlistDao.findById(id);
        PlaylistValidator.validatePlaylistExists(playlist);
        PlaylistValidator.validatePlaylistOwner(playlist, currentUserId, ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);

        List<PlaylistTrack> existingTrackList = playlistTrackDao.findByPlaylistId(id);
        PlaylistValidator.validateReorderTrackList(existingTrackList, requestTrackList);

        int maxPositionNo = existingTrackList.stream()
                .map(PlaylistTrack::getPositionNo)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        int temporaryPositionStart = maxPositionNo + requestTrackList.size() + 1;

        List<PlaylistTrack> temporaryPositionList = new ArrayList<>();
        for (int i = 0; i < requestTrackList.size(); i++) {
            temporaryPositionList.add(PlaylistTrackConvert.toPositionEntity(
                    requestTrackList.get(i).getPlaylistTrackId(), temporaryPositionStart + i));
        }
        playlistTrackDao.updatePlaylistTrackPositions(temporaryPositionList);

        List<PlaylistTrack> finalPositionList = new ArrayList<>();
        for (int i = 0; i < requestTrackList.size(); i++) {
            finalPositionList.add(PlaylistTrackConvert.toPositionEntity(
                    requestTrackList.get(i).getPlaylistTrackId(), i + 1));
        }
        playlistTrackDao.updatePlaylistTrackPositions(finalPositionList);

        return getPlaylistDetail(id, currentUserId);
    }

    @Transactional
    public PlaylistDetailResponseDto updatePlaylistComment(Long id, Long commentId, Long userId, Comment comment) {
        PlaylistValidator.validateCommentRequest(comment);

        Comment existingComment = commentDao.findById(commentId);
        PlaylistValidator.validateCommentExists(existingComment, id);
        PlaylistValidator.validateCommentOwner(existingComment, userId);

        existingComment.setContent(comment.getContent());
        commentDao.updateComment(existingComment);

        return getPlaylistDetail(id, userId);
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
    public PlaylistDetailResponseDto deletePlaylistTrack(Long id, Long currentUserId, Long trackId) {
        PlaylistValidator.validateTrackId(trackId);

        Playlist playlist = playlistDao.findById(id);
        PlaylistValidator.validatePlaylistExists(playlist);
        PlaylistValidator.validatePlaylistOwner(playlist, currentUserId, ErrorCode.PLAYLIST_UPDATE_FORBIDDEN);

        PlaylistValidator.validateTrackExists(playlistTrackDao.existsByPlaylistIdAndTrackId(id, trackId));

        int deletedCount = playlistTrackDao.deletePlaylistTrack(trackId);
        PlaylistValidator.validateTrackDeleteCount(deletedCount);

        playlistDao.decreaseTrackCount(id);

        compactPlaylistTrackPositions(id);

        return getPlaylistDetail(id, currentUserId);
    }

    @Transactional
    public PlaylistDetailResponseDto deletePlaylistComment(Long id, Long commentId, Long userId) {
        Comment existingComment = commentDao.findById(commentId);
        PlaylistValidator.validateCommentExists(existingComment, id);
        PlaylistValidator.validateCommentOwner(existingComment, userId);

        commentDao.deleteComment(commentId);
        playlistDao.decreaseCommentCount(id);

        return getPlaylistDetail(id, userId);
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private int getNextPositionNo(List<PlaylistTrack> playlistTrackList) {
        if (playlistTrackList == null || playlistTrackList.isEmpty()) {
            return 1;
        }

        return playlistTrackList.stream()
                .map(PlaylistTrack::getPositionNo)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private void compactPlaylistTrackPositions(Long playlistId) {
        List<PlaylistTrack> playlistTrackList = playlistTrackDao.findByPlaylistId(playlistId);
        List<PlaylistTrack> playlistTrackUpdateList = new ArrayList<>();

        for (int i = 0; i < playlistTrackList.size(); i++) {
            PlaylistTrack playlistTrack = playlistTrackList.get(i);
            int positionNo = i + 1;

            if (playlistTrack.getPositionNo() == null || playlistTrack.getPositionNo() != positionNo) {
                playlistTrack.setPositionNo(positionNo);
                playlistTrackUpdateList.add(playlistTrack);
            }
        }

        if (!playlistTrackUpdateList.isEmpty()) {
            playlistTrackDao.updatePlaylistTrackPositions(playlistTrackUpdateList);
        }
    }

    public List<PlaylistResponseDto> searchPlaylists(String keyword) {
        PlaylistValidator.validateSearchKeyword(keyword);
        String trimmedKeyword = keyword.trim();

        return playlistDao.findByKeywordPlaylist(trimmedKeyword).stream()
                .map(PlaylistConvert::toResponseDto)
                .collect(Collectors.toList());
    }
}
