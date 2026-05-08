package com.example.tune_share_hub_backend.convert;

import java.util.List;

import com.example.tune_share_hub_backend.dto.music.CommentResponseDto;
import com.example.tune_share_hub_backend.entity.Comment;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CommentConvert {

	public static CommentResponseDto toCommentResponseDto(Comment comment) {
		return CommentResponseDto.builder()
				.commentId(comment.getCommentId())
				.playlistId(comment.getPlaylistId())
				.userId(comment.getUserId())
				.userNickname(comment.getUserNickname())
				.content(comment.getContent())
				.createdAt(comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null)
				.updatedAt(comment.getUpdatedAt() != null ? comment.getUpdatedAt().toString() : null)
				.build();
	}

	public static List<CommentResponseDto> toCommentResponseDtoList(List<Comment> comments) {
		return comments.stream()
				.map(CommentConvert::toCommentResponseDto)
				.toList();
	}
}

