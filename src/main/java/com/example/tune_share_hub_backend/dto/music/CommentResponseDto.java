package com.example.tune_share_hub_backend.dto.music;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CommentResponseDto {
	private Long commentId;
	private Long playlistId;
	private Long userId;
	private String content;
	private String createdAt;
	private String updatedAt;

}
