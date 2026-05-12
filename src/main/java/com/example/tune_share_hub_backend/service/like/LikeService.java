package com.example.tune_share_hub_backend.service.like;

import com.example.tune_share_hub_backend.convert.PlaylistConvert;
import com.example.tune_share_hub_backend.dao.like.LikeDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistMapperDao;
import com.example.tune_share_hub_backend.dao.user.UserDao;
import com.example.tune_share_hub_backend.dto.like.LikeResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.entity.like.Like;
import com.example.tune_share_hub_backend.entity.user.User;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeDao likeDao;
    private final PlaylistMapperDao playlistMapper;
    private final UserDao userDao;

    @Transactional
    public LikeResponseDto like(Long playlistId, Long userId) {
        Playlist playlist = playlistMapper.findById(playlistId);
        if (playlist == null) {
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        }
        if ("N".equals(playlist.getPublicYn())) {
            throw new CustomException(ErrorCode.PRIVATE_PLAYLIST_CANNOT_BE_LIKED);
        }

        Like existingLike = likeDao.getLikeByUserIdAndPlaylistId(playlistId, userId);
        boolean isActionLike = existingLike == null || "N".equals(existingLike.getStatus());
        String newStatus = isActionLike ? "Y" : "N";

        if (existingLike == null) {
            likeDao.insertLike(playlistId, userId, newStatus);
        } else {
            likeDao.updateLikeStatus(playlistId, userId, newStatus);
        }

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

    @Transactional(readOnly = true)
    public List<PlaylistResponseDto> getLikedPlaylists(Long userId) {
        User user = userDao.getUserById(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return likeDao.getLikedPlaylistsByUserId(userId)
                .stream()
                .map(PlaylistConvert::toResponseDto)
                .toList();
    }
}
