package com.example.tune_share_hub_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlaylistRequestDto {
   
    @NotBlank(message = "제목은 필수입니다.")
    
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("cover_image_url")
    private String coverImageUrl;

    @JsonProperty("public_yn")
    private String publicYn = "Y";
}


