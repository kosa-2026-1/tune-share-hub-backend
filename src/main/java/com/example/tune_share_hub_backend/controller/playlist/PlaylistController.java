package com.example.tune_share_hub_backend.controller.playlist;

import com.example.tune_share_hub_backend.convert.CommentConvert;
import com.example.tune_share_hub_backend.convert.PlaylistTrackConvert;
import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.like.LikeResponseDto;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackCreateRequestDto;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackReorderRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.CommentRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistDetailResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.entity.Comment;
import com.example.tune_share_hub_backend.entity.PlaylistTrack;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import com.example.tune_share_hub_backend.global.interceptor.AccessTokenCheck;
import com.example.tune_share_hub_backend.global.interceptor.LoginUserId;
import com.example.tune_share_hub_backend.service.like.LikeService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor

public class PlaylistController {

    private final PlaylistService playlistService;
    private final LikeService likeService;

    @Operation(summary = "플레이리스트 수정", description = "로그인한 사용자가 본인 소유 플레이리스트를 수정합니다.")
    @AccessTokenCheck
    @PutMapping("/playlists/{id}")
    public ApiResponseDto<PlaylistDetailResponseDto> updatePlaylist(
            @PathVariable("id") Long playlistId,
            @RequestBody @Valid PlaylistRequestDto request,
            @LoginUserId Long userId) {
        PlaylistDetailResponseDto result = playlistService.updatePlaylist(playlistId, userId, request);
        return ApiResponseDto.success(result, "플레이리스트가 수정되었습니다.");
    }

    @Operation(summary = "공개/비공개 설정", description = "로그인한 사용자가 본인 소유 플레이리스트의 공개 여부를 변경합니다.")
    @PatchMapping("/playlists/{id}/visibility")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> updatePlaylistVisibility(
            @PathVariable("id") Long playlistId,
            @RequestBody PlaylistRequestDto request,
            @LoginUserId Long userId) {
        PlaylistDetailResponseDto result = playlistService.updatePlaylistVisibility(playlistId, userId, request);
        return ApiResponseDto.success(result, "플레이리스트 공개 여부가 변경되었습니다.");
    }

    @Operation(summary = "플레이리스트 생성", description = "로그인한 사용자가 새 플레이리스트를 생성합니다.")
    @PostMapping("/playlists")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> create(
            @RequestBody @Valid PlaylistRequestDto req,
            @LoginUserId Long userId) {
        PlaylistDetailResponseDto result = playlistService.create(userId, req);
        return ApiResponseDto.success(result, "플레이리스트 생성 성공");
    }

    @Operation(summary = "플레이리스트 삭제", description = "로그인한 사용자가 본인 소유 플레이리스트를 삭제합니다.")
    @AccessTokenCheck
    @DeleteMapping("/playlists/{id}")
    public ApiResponseDto<Void> deletePlaylist(
            @PathVariable("id") Long playlistId,
            @LoginUserId Long userId) {
        playlistService.deletePlaylist(playlistId, userId);
        return ApiResponseDto.success(null, "플레이리스트가 삭제되었습니다.");
    }

    @Operation(summary = "플레이리스트 복사", description = "로그인한 사용자가 공개 플레이리스트를 복사합니다.")
    @AccessTokenCheck
    @PostMapping("/playlists/{id}/copy")
    public ApiResponseDto<PlaylistDetailResponseDto> copyPlaylist(
            @PathVariable("id") Long playlistId,
            @LoginUserId Long userId) {
        PlaylistDetailResponseDto result = playlistService.copyPlaylist(playlistId, userId);
        return ApiResponseDto.success(result, "플레이리스트가 복사되었습니다.");
    }

    @Operation(summary = "공개 목록 조회", description = "공개된 플레이리스트를 페이지 단위로 조회합니다.")
    @GetMapping("/playlists")
    @AccessTokenCheck(required = false)
    public ApiResponseDto<Map<String, Object>> getPublicPlaylists(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true)
            @LoginUserId(required = false ) Long userId) {
        Map<String, Object> result = playlistService.getPublicPlaylists(page, size, userId);
        return ApiResponseDto.success(result, "공개 플레이리스트 목록 조회 성공");
    }

    @Operation(summary = "내 플레이리스트 목록", description = "로그인한 사용자의 전체 플레이리스트를 조회합니다.")
    @GetMapping("/users/me/playlists")
    @AccessTokenCheck
    public ApiResponseDto<List<PlaylistResponseDto>> getMyPlaylists(@LoginUserId Long userId) {
        List<PlaylistResponseDto> result = playlistService.getMyPlaylists(userId);
        return ApiResponseDto.success(result, "조회 성공");
    }

    @Operation(summary = "플레이리스트 상세 조회", description = "플레이리스트 ID로 상세 정보를 조회합니다.")
    @GetMapping("/playlists/{id}")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> getPlaylistDetail(
            @PathVariable Long id,
            @LoginUserId Long userId) {
        playlistService.increaseViewCount(id);
        PlaylistDetailResponseDto result = playlistService.getPlaylist(id, userId);
        return ApiResponseDto.success(result, "조회 성공");
    }

    @Operation(summary = "플레이리스트에 트랙 추가", description = "로그인한 사용자가 본인 소유 플레이리스트에 하나 이상의 트랙을 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "트랙 추가 성공"),
            @ApiResponse(responseCode = "400", description = "요청 본문이 비어 있거나 형식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 플레이리스트에는 트랙을 추가할 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없습니다.")
    })
    @PostMapping("/playlists/{id}/tracks")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> addTrackToPlaylist(
            @Parameter(description = "트랙을 추가할 플레이리스트 ID", example = "1", required = true) @PathVariable Long id,
            @LoginUserId Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "추가할 트랙 목록", required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistTrackCreateRequestDto.class)))) @RequestBody List<PlaylistTrackCreateRequestDto> requestListDto) {
        if (requestListDto == null || requestListDto.isEmpty() || requestListDto.contains(null)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        List<PlaylistTrack> playlistTracksList = PlaylistTrackConvert.toEntities(requestListDto, id);
        PlaylistDetailResponseDto result = playlistService.addTrackToPlaylist(id, userId, playlistTracksList);
        return ApiResponseDto.success(result, "트랙이 플레이리스트에 추가되었습니다.");
    }

    @Operation(summary = "플레이리스트 트랙 삭제", description = "로그인한 사용자가 본인 소유 플레이리스트에서 특정 트랙을 제거합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "트랙 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "요청한 트랙 ID 형식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 플레이리스트의 트랙은 삭제할 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "플레이리스트 또는 트랙을 찾을 수 없습니다.")
    })
    @DeleteMapping("/playlists/{id}/tracks/{trackId}")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> removeTrackFromPlaylist(
            @LoginUserId Long userId,
            @Parameter(description = "트랙을 삭제할 플레이리스트 ID", example = "1", required = true) @PathVariable Long id,
            @Parameter(description = "삭제할 플레이리스트 트랙 ID", example = "10", required = true) @PathVariable Long trackId) {
        PlaylistDetailResponseDto result = playlistService.removeTrackFromPlaylist(id, userId, trackId);
        return ApiResponseDto.success(result, "트랙이 플레이리스트에서 제거되었습니다.");
    }

    @Operation(summary = "플레이리스트 트랙 순서 변경", description = "프론트에서 드래그앤드랍 후 전달한 트랙 목록의 배열 순서대로 POSITION_NO를 1부터 다시 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "트랙 순서 변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청한 트랙 순서 목록이 올바르지 않습니다."),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 플레이리스트의 트랙 순서는 변경할 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "플레이리스트 또는 트랙을 찾을 수 없습니다.")
    })
    @PatchMapping("/playlists/{id}/tracks/reorder")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> reorderPlaylistTracks(
            @Parameter(description = "트랙 순서를 변경할 플레이리스트 ID", example = "1", required = true) @PathVariable Long id,
            @LoginUserId Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "드래그앤드랍 후 새 순서대로 정렬된 플레이리스트 트랙 목록", required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistTrackReorderRequestDto.class)))) @RequestBody List<PlaylistTrackReorderRequestDto> requestListDto) {
        PlaylistDetailResponseDto result = playlistService.reorderTrack(id, userId, requestListDto);
        return ApiResponseDto.success(result, "트랙 순서가 변경되었습니다.");
    }

    @Operation(summary = "플레이리스트 좋아요/취소", description = "로그인한 사용자가 플레이리스트에 좋아요를 누르거나 취소합니다.")
    @PostMapping("/playlists/{id}/likes")
    @AccessTokenCheck
    public ApiResponseDto<LikeResponseDto> like(
            @PathVariable Long id,
            @LoginUserId Long userId) {
        LikeResponseDto likeResponseDto = likeService.like(id, userId);

        return ApiResponseDto.success(likeResponseDto, "플레이리스트 좋아요 상태 변경 성공");
    }


    @Operation(
            summary = "플레이리스트 댓글 작성",
            description = "로그인한 사용자가 플레이리스트에 댓글을 작성합니다. 비공개 플레이리스트에는 소유자만 댓글을 작성할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 본문이 비어 있거나 댓글 내용이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "액세스 토큰이 유효하지 않습니다."),
            @ApiResponse(responseCode = "404", description = "플레이리스트 또는 사용자를 찾을 수 없습니다.")
    })
    @PostMapping("playlists/{id}/comments")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> createCommentToPlaylist(
            @Parameter(description = "댓글을 작성할 플레이리스트 ID", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "작성할 댓글 내용",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CommentRequestDto.class))
            )
            @RequestBody CommentRequestDto requestDto,
            @Parameter(hidden = true)
            @LoginUserId Long userId
    ) {
        if (requestDto == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Comment comment = CommentConvert.toEntity(requestDto);
        PlaylistDetailResponseDto result = playlistService.createComment(id, userId, comment);
        return ApiResponseDto.success(result, "댓글이 추가되었습니다.");
    }

    @Operation(
            summary = "플레이리스트 댓글 수정",
            description = "로그인한 사용자가 본인이 작성한 플레이리스트 댓글 내용을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 본문이 비어 있거나 댓글 내용이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "액세스 토큰이 유효하지 않습니다."),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다.")
    })
    @PutMapping("playlists/{id}/comments/{commentId}")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> updateCommentToPlaylist(
            @Parameter(description = "댓글이 속한 플레이리스트 ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "수정할 댓글 ID", example = "10", required = true)
            @PathVariable Long commentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 댓글 내용",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CommentRequestDto.class))
            )
            @RequestBody CommentRequestDto requestDto,
            @Parameter(hidden = true)
            @LoginUserId Long userId) {
        if (requestDto == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Comment comment = CommentConvert.toEntity(requestDto);
        PlaylistDetailResponseDto result = playlistService.updateComment(id, commentId, userId, comment);
        return ApiResponseDto.success(result, "댓글이 수정되었습니다.");
    }

    @Operation(
            summary = "플레이리스트 댓글 삭제",
            description = "로그인한 사용자가 본인이 작성한 플레이리스트 댓글을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "액세스 토큰이 유효하지 않습니다."),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다.")
    })
    @DeleteMapping("playlists/{id}/comments/{commentId}")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> deleteCommentToPlaylist(
            @Parameter(description = "댓글이 속한 플레이리스트 ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "삭제할 댓글 ID", example = "10", required = true)
            @PathVariable Long commentId,
            @Parameter(hidden = true)
            @LoginUserId Long userId) {
        PlaylistDetailResponseDto result = playlistService.deleteComment(id, commentId, userId);
        return ApiResponseDto.success(result, "댓글이 삭제되었습니다.");
    }

    @Operation(summary = "인기 플레이리스트 랭킹", description = "좋아요 수 기준 공개 플레이리스트 랭킹을 조회합니다.")
    @GetMapping("/playlists/ranking")
    @AccessTokenCheck(required = false)
    public ApiResponseDto<List<PlaylistResponseDto>> getPlaylistRanking(
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(hidden = true)
            @LoginUserId(required = false) Long userId) {
        List<PlaylistResponseDto> result = playlistService.getPlaylistRanking(limit, userId);
        return ApiResponseDto.success(result, "인기 플레이리스트 랭킹 조회 성공");
    }
}
