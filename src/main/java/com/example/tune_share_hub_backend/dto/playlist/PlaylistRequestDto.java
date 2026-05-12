package com.example.tune_share_hub_backend.dto.playlist;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class PlaylistRequestDto {

    @NotBlank(message = "제목은 필수입니다.")

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonAlias("coverImageUrl")
    @JsonProperty("cover_image_url")
    private String coverImageUrl;

    @JsonAlias("publicYn")
    @JsonProperty("public_yn")
    private String publicYn;

    @Size(max = 5, message = "태그는 최대 5개까지만 등록 가능합니다.")
    @JsonProperty("tags")
    private List<@NotBlank(message = "태그는 비어 있을 수 없습니다.") @Size(max = 20, message = "태그는 20자 이내여야 합니다.") String> tags;

}


