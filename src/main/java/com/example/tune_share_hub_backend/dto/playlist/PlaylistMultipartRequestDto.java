package com.example.tune_share_hub_backend.dto.playlist;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "플레이리스트 생성/수정 multipart 요청")
public class PlaylistMultipartRequestDto {

    @Schema(description = "플레이리스트 제목", example = "운동할 때 듣는 노래", requiredMode = Schema.RequiredMode.REQUIRED)
    public String title;

    @Schema(description = "플레이리스트 설명", example = "헬스할 때 듣기 좋은 노래 모음")
    public String description;

    @Schema(description = "공개 여부", example = "Y")
    public String publicYn;

    @Schema(description = "태그 목록", example = "[\"운동\", \"힙합\"]")
    public List<String> tags;

    @Schema(description = "플레이리스트 커버 이미지", type = "string", format = "binary")
    public String coverImage;
}
