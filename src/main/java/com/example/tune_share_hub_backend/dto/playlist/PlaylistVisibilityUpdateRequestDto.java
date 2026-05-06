package com.example.tune_share_hub_backend.dto.playlist;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaylistVisibilityUpdateRequestDto {
    // 공개 여부 값이다. Y이면 공개, N이면 비공개이다.
    private String publicYn;
}
