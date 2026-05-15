package com.example.tune_share_hub_backend.dto.like;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(description = "플레이리스트 좋아요 응답")
public class LikeResponseDto {
    @Schema(description = "플레이리스트 ID", example = "1")
    private Long playlistId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "좋아요 상태", example = "LIKE")
    private String status;

    @Schema(description = "플레이리스트 전체 좋아요 수", example = "12")
    private int totalLikeCount;
}
