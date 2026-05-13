package com.example.tune_share_hub_backend.service.like;

import com.example.tune_share_hub_backend.convert.LikeConvert;
import com.example.tune_share_hub_backend.convert.PlaylistConvert;
import com.example.tune_share_hub_backend.dao.like.LikeDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistDao;
import com.example.tune_share_hub_backend.dto.like.LikeResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.entity.like.Like;
import com.example.tune_share_hub_backend.validate.LikeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeDao likeDao;
    private final PlaylistDao playlistMapper;

    @Transactional
    public LikeResponseDto like(Long playlistId, Long userId) {
        Playlist playlist = playlistMapper.findById(playlistId);
        LikeValidator.validateLikablePlaylist(playlist);

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

        return LikeConvert.toResponseDto(playlistId, userId, newStatus, updatedPlaylist);
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponseDto> getLikedPlaylistList(Long userId) {
        return likeDao.getLikedPlaylistListByUserId(userId)
                .stream()
                .map(PlaylistConvert::toResponseDto)
                .toList();
    }
}
