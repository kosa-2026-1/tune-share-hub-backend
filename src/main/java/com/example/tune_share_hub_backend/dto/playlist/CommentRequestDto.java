package com.example.tune_share_hub_backend.dto.playlist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {
	@Schema(description = "댓글 내용", example = "좋은 플레이리스트네요!")
	private String content;
}
