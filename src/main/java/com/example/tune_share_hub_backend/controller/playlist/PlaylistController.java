package com.example.tune_share_hub_backend.controller.playlist;

import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistVisibilityUpdateRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistUpdateRequestDto;
import com.example.tune_share_hub_backend.service.playlist.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {
    // 플레이리스트 관련 비즈니스 로직을 처리하는 서비스이다.
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    // 1-1. 플레이리스트 제목, 설명, 커버 이미지, 공개 여부를 한 번에 수정한다.
    @Operation(summary = "플레이리스트 수정", description = "로그인한 사용자가 본인 소유 플레이리스트를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> updatePlaylist(
            // URL의 {id} 값을 playlistId 매개변수로 받는다.
            @PathVariable("id") Long playlistId,
            // 요청 JSON body를 PlaylistUpdateRequestDto 객체로 변환해서 받는다.
            @RequestBody PlaylistUpdateRequestDto request
    ) {
        // 현재는 인증 기능이 없으므로 USER_ID가 1인 사용자로 테스트한다.
        // TODO: JWT 인증 기능이 완성되면 토큰에서 로그인 사용자 ID를 꺼내도록 변경한다.
        Long userId = 1L;
        playlistService.updatePlaylist(playlistId, userId, request);
        return ResponseEntity.ok(ApiResponseDto.success(null, "플레이리스트가 수정되었습니다."));
    }

    // 1-3. 플레이리스트의 공개 여부만 별도로 변경한다.
    @Operation(summary = "플레이리스트 공개 여부 설정", description = "로그인한 사용자가 본인 소유 플레이리스트의 공개 여부를 변경합니다.")
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<ApiResponseDto<Void>> updatePlaylistVisibility(
            // URL의 {id} 값을 playlistId 매개변수로 받는다.
            @PathVariable("id") Long playlistId,
            // 요청 JSON body를 PlaylistVisibilityUpdateRequestDto 객체로 변환해서 받는다.
            @RequestBody PlaylistVisibilityUpdateRequestDto request
    ) {
        // 현재는 인증 기능이 없으므로 USER_ID가 1인 사용자로 테스트한다.
        // TODO: JWT 인증 기능이 완성되면 토큰에서 로그인 사용자 ID를 꺼내도록 변경한다.
        Long userId = 1L;
        playlistService.updatePlaylistVisibility(playlistId, userId, request);
        return ResponseEntity.ok(ApiResponseDto.success(null, "플레이리스트 공개 여부가 변경되었습니다."));
    }
}
