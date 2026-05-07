package com.example.tune_share_hub_backend.controller.playlist;

import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistRequestDto;
import com.example.tune_share_hub_backend.service.playlist.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    private Long getCurrentUserId() {
        return 1L; // 임시 하드코딩(user에 1, test로 넣어놓음.)
    }

    @Operation(summary = "플레이리스트 수정", description = "로그인한 사용자가 본인 소유 플레이리스트를 수정합니다.")
    @PutMapping("/playlists/{id}")
    public ResponseEntity<ApiResponseDto<Void>> updatePlaylist(
            @PathVariable("id") Long playlistId,
            @RequestBody PlaylistRequestDto request
    ) {
        playlistService.updatePlaylist(playlistId, getCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponseDto.success(null, "플레이리스트가 수정되었습니다."));
    }

    @Operation(summary = "플레이리스트 공개 여부 설정", description = "로그인한 사용자가 본인 소유 플레이리스트의 공개 여부를 변경합니다.")
    @PatchMapping("/playlists/{id}/visibility")
    public ResponseEntity<ApiResponseDto<Void>> updatePlaylistVisibility(
            @PathVariable("id") Long playlistId,
            @RequestBody PlaylistRequestDto request
    ) {
        playlistService.updatePlaylistVisibility(playlistId, getCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponseDto.success(null, "플레이리스트 공개 여부가 변경되었습니다."));
    }

    @PostMapping("/playlists")
    public ResponseEntity<?> create(@RequestBody @Valid PlaylistRequestDto req) {
        PlaylistResponseDto result = playlistService.create(getCurrentUserId(), req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result,
                "message", "플레이리스트 생성 성공"
        ));
    }

    @GetMapping("/users/me/playlists")
    public ResponseEntity<?> getMyPlaylists() {
        List<PlaylistResponseDto> result = playlistService.getMyPlaylists(getCurrentUserId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result,
                "message", "조회 성공"
        ));
    }

    @GetMapping("/playlists/{id}")
    public ResponseEntity<?> getPlaylist(@PathVariable Long id) {
        PlaylistResponseDto result = playlistService.getPlaylist(id, getCurrentUserId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result,
                "message", "조회 성공"
        ));
    }
}
