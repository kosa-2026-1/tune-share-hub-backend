package com.example.tune_share_hub_backend.entity.refresh;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refresh {
    private Long tokenId;
    private Long userId;
    private String tokenValue;
    private Boolean revokedYn;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime revokedAt;
}
