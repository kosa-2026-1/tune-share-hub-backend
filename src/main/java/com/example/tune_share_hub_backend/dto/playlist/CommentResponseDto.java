package com.example.tune_share_hub_backend.dto.playlist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Schema(description = "플레이리스트 댓글 응답")
public class CommentResponseDto {
	@Schema(description = "댓글 ID", example = "10")
	private Long commentId;

	@Schema(description = "플레이리스트 ID", example = "1")
	private Long playlistId;

	@Schema(description = "작성자 사용자 ID", example = "1")
	private Long userId;

	@Schema(description = "작성자 닉네임", example = "테스트유저")
	private String userNickname;

	@Schema(description = "댓글 내용", example = "좋은 플레이리스트네요!")
	private String content;

	@Schema(description = "댓글 작성 일시", example = "2026-05-14T10:30:00")
	private String createdAt;

	@Schema(description = "댓글 수정 일시", example = "2026-05-14T10:35:00")
	private String updatedAt;

}
