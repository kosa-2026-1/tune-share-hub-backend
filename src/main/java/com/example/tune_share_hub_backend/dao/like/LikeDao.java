package com.example.tune_share_hub_backend.dao.like;

import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.entity.like.Like;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LikeDao {
    int incrementLikeCount(Long playlistId);
    int decrementLikeCount(Long playlistId);
    List<Playlist> getLikedPlaylistsByUserId(Long userId);
    Like getLikeByUserIdAndPlaylistId(Long playlistId, Long userId);
    int insertLike(Long playlistId, Long userId, String status);
    int updateLikeStatus(Long playlistId,Long userId, String status);
}

