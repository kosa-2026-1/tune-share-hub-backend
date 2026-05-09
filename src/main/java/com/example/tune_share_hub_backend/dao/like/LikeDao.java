package com.example.tune_share_hub_backend.dao.like;

import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.entity.like.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LikeDao {
    int incrementLikeCount(Long playlistId);
    int decrementLikeCount(Long playlistId);
    List<Playlist> getLikedPlaylistsByUserId(Long userId);
    Like getLikeByUserIdAndPlaylistId(@Param("playlistId") Long playlistId, @Param("userId") Long userId);
    int insertLike(@Param("playlistId") Long playlistId, @Param("userId") Long userId, @Param("status") String status);
    int updateLikeStatus(@Param("playlistId") Long playlistId, @Param("userId") Long userId, @Param("status") String status);
}
