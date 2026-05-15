package com.example.tune_share_hub_backend.dto.playlist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Schema(description = "플레이리스트 목록 응답")
public class PlaylistResponseDto {

    @Schema(description = "플레이리스트 ID", example = "1")
    private Long playlistId;

    @Schema(description = "플레이리스트 제목", example = "밤 드라이브 플레이리스트")
    private String title;

    @Schema(description = "플레이리스트 설명", example = "퇴근길에 듣기 좋은 감성 음악 모음")
    private String description;

    @Schema(description = "공개 여부", example = "Y")
    private String publicYn;

    @Schema(description = "조회 수", example = "128")
    private int viewCount;

    @Schema(description = "좋아요 수", example = "12")
    private int likeCount;

    @Schema(description = "트랙 수", example = "5")
    private int trackCount;

    @Schema(description = "커버 이미지 URL", example = "/uploads/images/night-drive-cover.jpg")
    private String coverImageUrl;

    @Schema(description = "댓글 수", example = "3")
    private int commentCount;

    @Schema(description = "생성 일시", example = "2026-05-14T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "태그 목록", example = "[\"드라이브\", \"감성\"]")
    private List<String> tags;


}
