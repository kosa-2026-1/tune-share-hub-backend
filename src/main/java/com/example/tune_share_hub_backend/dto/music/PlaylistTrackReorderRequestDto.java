package com.example.tune_share_hub_backend.dto.music;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "플레이리스트 트랙 순서 변경 요청")
public class PlaylistTrackReorderRequestDto {
	@Schema(description = "플레이리스트 트랙 ID", example = "5")
	private Long playlistTrackId;

	@Schema(description = "현재 트랙 순서. 순서 변경 저장 시에는 배열 순서를 기준으로 다시 계산됩니다.", example = "2")
	private Integer positionNo;
}
