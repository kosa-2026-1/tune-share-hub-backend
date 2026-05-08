package com.example.tune_share_hub_backend.dao.playlist;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.tune_share_hub_backend.entity.Comment;

@Mapper
public interface CommentDao {
    List<Comment> findByPlaylistId(Long playlistId);
}
