package com.example.tune_share_hub_backend.controller.playlist;

import com.example.tune_share_hub_backend.convert.PlaylistTrackConvert;
import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.like.LikeResponseDto;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackCreateRequestDto;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackReorderRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.entity.PlaylistTrack;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import com.example.tune_share_hub_backend.global.interceptor.AccessTokenCheck;
import com.example.tune_share_hub_backend.global.interceptor.LoginUserId;
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
import org.springframework.web.bind.annotation.*;

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

    @Operation(
            summary = "플레이리스트 트랙 삭제",
            description = "로그인한 사용자가 본인 소유 플레이리스트에서 특정 트랙을 제거합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "트랙 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "요청한 트랙 ID 형식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 플레이리스트의 트랙은 삭제할 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "플레이리스트 또는 트랙을 찾을 수 없습니다.")
    })
    @DeleteMapping("/playlists/{id}/tracks/{trackId}")
    public ResponseEntity<?> removeTrackFromPlaylist(
            @Parameter(description = "트랙을 삭제할 플레이리스트 ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "삭제할 플레이리스트 트랙 ID", example = "10", required = true)
            @PathVariable Long trackId
    ) {
        playlistService.removeTrackFromPlaylist(id, getCurrentUserId(), trackId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "트랙이 플레이리스트에서 제거되었습니다."
        ));
    }

    @Operation(
            summary = "플레이리스트 트랙 순서 변경",
            description = "프론트에서 드래그앤드랍 후 전달한 트랙 목록의 배열 순서대로 POSITION_NO를 1부터 다시 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "트랙 순서 변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청한 트랙 순서 목록이 올바르지 않습니다."),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 플레이리스트의 트랙 순서는 변경할 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "플레이리스트 또는 트랙을 찾을 수 없습니다.")
    })
    @PatchMapping("/playlists/{id}/tracks/reorder")
    public ResponseEntity<?> reorderPlaylistTracks(
            @Parameter(description = "트랙 순서를 변경할 플레이리스트 ID", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "드래그앤드랍 후 새 순서대로 정렬된 플레이리스트 트랙 목록",
                    required = true,
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = PlaylistTrackReorderRequestDto.class))
                    )
            )
            @RequestBody List<PlaylistTrackReorderRequestDto> requestListDto
    ) {
        playlistService.reorderTrack(id, getCurrentUserId(), requestListDto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "트랙 순서가 변경되었습니다."
        ));
    }

    @Operation(summary = "플레이리스트 좋아요/취소", description = "로그인한 사용자가 플레이리스트에 좋아요를 누르거나 취소합니다.")
    @PostMapping("/playlists/{id}/likes")
    @AccessTokenCheck
    public ResponseEntity<LikeResponseDto> like(
            @PathVariable Long id,
            @LoginUserId Long userId) {
        LikeResponseDto likeResponseDto = playlistService.like(id, userId);

        return ResponseEntity.ok(likeResponseDto);
    }



}
