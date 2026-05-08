package com.example.tune_share_hub_backend.dao.playlist;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.tune_share_hub_backend.entity.Comment;

@Mapper
public interface CommentDao {
	void insertComment(@Param("comment")Comment comment);
}
