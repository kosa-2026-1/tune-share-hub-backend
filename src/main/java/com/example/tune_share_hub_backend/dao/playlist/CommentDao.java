package com.example.tune_share_hub_backend.dao.playlist;

import com.example.tune_share_hub_backend.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentDao {

    void insertComment(@Param("comment") Comment comment);

    Comment findById(@Param("commentId") Long commentId);

    void updateComment(@Param("comment") Comment existingComment);

    void deleteComment(@Param("commentId") Long commentId);

    int deleteByPlaylistId(@Param("playlistId") Long playlistId);

    List<Comment> findByPlaylistId(Long playlistId);
}
