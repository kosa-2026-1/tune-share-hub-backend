package com.example.tune_share_hub_backend.dao.like;

import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.entity.like.Like;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LikeDao {
    public int insert(Like like);
    public int incrementLikeCount(Long playlistId);
    public int decrementLikeCount(Long playlistId);
    public int updateStatus(Long likeId, String status);
    public List<Playlist> getLikedPlaylistsByUserId(Long userId);
}

