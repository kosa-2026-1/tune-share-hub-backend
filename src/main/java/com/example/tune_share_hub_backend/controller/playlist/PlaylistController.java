package com.example.tune_share_hub_backend.controller.playlist;

import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistUpdateRequestDto;
import com.example.tune_share_hub_backend.service.playlist.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @Operation(summary = "플레이리스트 수정", description = "로그인한 사용자가 본인 소유 플레이리스트를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> updatePlaylist(
            // URL의 {id} 값을 playlistId 매개변수로 받는다.
            @PathVariable("id") Long playlistId,
            // 요청 JSON body를 PlaylistUpdateRequestDto 객체로 변환해서 받는다.
            @RequestBody PlaylistUpdateRequestDto request
    ) {
        // TODO: JWT 인증 기능이 완성되면 토큰에서 로그인 사용자 ID를 꺼내도록 변경한다.
        Long userId = 1L;
        playlistService.updatePlaylist(playlistId, userId, request);
        return ResponseEntity.ok(ApiResponseDto.success(null, "플레이리스트가 수정되었습니다."));
    }
}
