package com.example.tune_share_hub_backend.dto.playlist;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaylistUpdateRequestDto {
    private String title;
    private String description;
    private String coverImageUrl;
    private String publicYn;
}
