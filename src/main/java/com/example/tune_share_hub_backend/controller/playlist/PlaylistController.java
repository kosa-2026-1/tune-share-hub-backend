package com.example.tune_share_hub_backend.controller.playlist;

import com.example.tune_share_hub_backend.convert.PlaylistTrackConvert;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackCreateRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistRequestDto;
import com.example.tune_share_hub_backend.entity.PlaylistTrack;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import com.example.tune_share_hub_backend.service.playlist.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "플레이리스트에 트랙 추가",
            description = "로그인한 사용자가 본인 소유 플레이리스트에 하나 이상의 트랙을 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "트랙 추가 성공"),
            @ApiResponse(responseCode = "400", description = "요청 본문이 비어 있거나 형식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 플레이리스트에는 트랙을 추가할 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없습니다.")
    })
    @PostMapping("/playlists/{id}/tracks")
    public ResponseEntity<?> addTrackToPlaylist(
            @Parameter(description = "트랙을 추가할 플레이리스트 ID", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "추가할 트랙 목록",
                    required = true,
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = PlaylistTrackCreateRequestDto.class))
                    )
            )
            @RequestBody List<PlaylistTrackCreateRequestDto> requestListDto
    ) {
        if (requestListDto == null || requestListDto.isEmpty() || requestListDto.contains(null)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        List<PlaylistTrack> playlistTracksList = PlaylistTrackConvert.toEntities(requestListDto, id);
        List<PlaylistTrack> newPlaylistTracksList = playlistService.addTrackToPlaylist(id, getCurrentUserId(), playlistTracksList);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "트랙이 플레이리스트에 추가되었습니다.",
                "data", PlaylistTrackConvert.toResponseDtoList(newPlaylistTracksList)
        ));
    }

}
