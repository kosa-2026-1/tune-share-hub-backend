package com.example.tune_share_hub_backend.controller.playlist;

import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.global.config.security.JwtProvider;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistRequestDto;
import com.example.tune_share_hub_backend.service.playlist.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    private final JwtProvider jwtProvider;

    private Long getCurrentUserId(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        String accessToken = authorization.substring(7);
        return jwtProvider.getUserId(accessToken);
    }

    @Operation(summary = "플레이리스트 수정", description = "로그인한 사용자가 본인 소유 플레이리스트를 수정합니다.")
    @PutMapping("/playlists/{id}")
    public ResponseEntity<ApiResponseDto<Void>> updatePlaylist(
            @PathVariable("id") Long playlistId,
            @RequestBody PlaylistRequestDto request,
            HttpServletRequest httpRequest) {
        playlistService.updatePlaylist(playlistId, getCurrentUserId(httpRequest), request);
        return ResponseEntity.ok(ApiResponseDto.success(null, "플레이리스트가 수정되었습니다."));
    }

    @Operation(summary = "플레이리스트 공개 여부 설정", description = "로그인한 사용자가 본인 소유 플레이리스트의 공개 여부를 변경합니다.")
    @PatchMapping("/playlists/{id}/visibility")
    public ResponseEntity<ApiResponseDto<Void>> updatePlaylistVisibility(
            @PathVariable("id") Long playlistId,
            @RequestBody PlaylistRequestDto request,
            HttpServletRequest httpRequest) {
        playlistService.updatePlaylistVisibility(playlistId, getCurrentUserId(httpRequest), request);
        return ResponseEntity.ok(ApiResponseDto.success(null, "플레이리스트 공개 여부가 변경되었습니다."));
    }

    @Operation(summary = "플레이리스트 생성", description = "로그인한 사용자가 새 플레이리스트를 생성합니다.")
    @PostMapping("/playlists")
    public ResponseEntity<ApiResponseDto<PlaylistResponseDto>> create(
            @RequestBody @Valid PlaylistRequestDto req,
            HttpServletRequest httpRequest) {
        PlaylistResponseDto result = playlistService.create(getCurrentUserId(httpRequest), req);
        return ResponseEntity.ok(ApiResponseDto.success(result, "플레이리스트 생성 성공"));
    }

    @Operation(summary = "공개 플레이리스트 목록 조회", description = "공개된 플레이리스트를 페이지 단위로 조회합니다.")
    @GetMapping("/playlists")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getPublicPlaylists(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = playlistService.getPublicPlaylists(page, size);
        return ResponseEntity.ok(ApiResponseDto.success(result, "공개 플레이리스트 목록 조회 성공"));
    }

    @Operation(summary = "내 플레이리스트 목록 조회", description = "로그인한 사용자의 전체 플레이리스트를 조회합니다.")
    @GetMapping("/users/me/playlists")
    public ResponseEntity<ApiResponseDto<List<PlaylistResponseDto>>> getMyPlaylists(HttpServletRequest request) {
        List<PlaylistResponseDto> result = playlistService.getMyPlaylists(getCurrentUserId(request));
        return ResponseEntity.ok(ApiResponseDto.success(result, "조회 성공"));
    }

    @Operation(summary = "플레이리스트 단건 조회", description = "플레이리스트 ID로 상세 정보를 조회합니다.")
    @GetMapping("/playlists/{id}")
    public ResponseEntity<ApiResponseDto<PlaylistResponseDto>> getPlaylist(@PathVariable Long id,
            HttpServletRequest request) {
        PlaylistResponseDto result = playlistService.getPlaylist(id, getCurrentUserId(request));
        return ResponseEntity.ok(ApiResponseDto.success(result, "조회 성공"));
    }
}
