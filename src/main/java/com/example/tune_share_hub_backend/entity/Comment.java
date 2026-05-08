package com.example.tune_share_hub_backend.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
@AllArgsConstructor
public class Comment {
	private Long commentId;
	private Long playlistId;
	private Long userId;
	private String userNickname;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
