package com.example.tune_share_hub_backend.dto.playlist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "플레이리스트 생성 요청")
public class PlaylistRequestDto {

    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "플레이리스트 제목", example = "운동할 때 듣는 노래")
    private String title;

    @Schema(description = "플레이리스트 설명", example = "헬스할 때 듣기 좋은 노래 모음")
    private String description;

    @Schema(description = "플레이리스트 커버 이미지", type = "string", format = "binary")
    private MultipartFile coverImage;

    @Schema(description = "공개 여부", example = "Y")
    private String publicYn;

    @Size(max = 5, message = "태그는 최대 5개까지만 등록 가능합니다.")
    @Schema(description = "태그 목록", example = "[\"운동\", \"힙합\"]")
    private List<
            @NotBlank(message = "태그는 비어 있을 수 없습니다.")
            @Size(max = 20, message = "태그는 20자 이내여야 합니다.")
                    String
            > tags;
}