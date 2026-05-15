package com.example.tune_share_hub_backend.controller.playlist;

import com.example.tune_share_hub_backend.convert.CommentConvert;
import com.example.tune_share_hub_backend.convert.PlaylistConvert;
import com.example.tune_share_hub_backend.convert.PlaylistTrackConvert;
import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.like.LikeResponseDto;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackCreateRequestDto;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackReorderRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.CommentRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistDetailResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistMultipartRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.global.interceptor.AccessTokenCheck;
import com.example.tune_share_hub_backend.global.interceptor.LoginUserId;
import com.example.tune_share_hub_backend.global.exception.dto.ApiError;
import com.example.tune_share_hub_backend.service.like.LikeService;
import com.example.tune_share_hub_backend.service.playlist.PlaylistService;
import com.example.tune_share_hub_backend.validate.PlaylistValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Playlist", description = "플레이리스트 API")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final LikeService likeService;

    @Operation(
            summary = "플레이리스트 생성",
            description = "로그인한 사용자가 새 플레이리스트를 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = PlaylistMultipartRequestDto.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플레이리스트 생성 성공", content = @Content(schema = @Schema(implementation = PlaylistDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping(value = "/playlists", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> createPlaylist(
            @Valid @ModelAttribute PlaylistRequestDto request,
            @Parameter(
                    description = "플레이리스트 커버 이미지",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @Parameter(hidden = true)
            @LoginUserId Long userId) {
        PlaylistDetailResponseDto result = playlistService.createPlaylist(
                userId, PlaylistConvert.toEntity(request, userId), coverImage);
        return ApiResponseDto.success(result, "플레이리스트 생성 성공");
    }

    @Operation(summary = "플레이리스트 복사", description = "로그인한 사용자가 공개 플레이리스트를 복사합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플레이리스트 복사 성공", content = @Content(schema = @Schema(implementation = PlaylistDetailResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "비공개 플레이리스트는 복사할 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @AccessTokenCheck
    @PostMapping("/playlists/{id}/copy")
    public ApiResponseDto<PlaylistDetailResponseDto> copyPlaylist(
            @Parameter(description = "복사할 플레이리스트 ID", example = "1", required = true)
            @PathVariable("id") Long playlistId,
            @Parameter(hidden = true)
            @LoginUserId Long userId) {
        PlaylistDetailResponseDto result = playlistService.copyPlaylist(playlistId, userId);
        return ApiResponseDto.success(result, "플레이리스트가 복사되었습니다.");
    }

    @Operation(summary = "플레이리스트에 트랙 추가", description = "로그인한 사용자가 본인 소유 플레이리스트에 하나 이상의 트랙을 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "트랙 추가 성공", content = @Content(schema = @Schema(implementation = PlaylistDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 본문이 비어 있거나 형식이 올바르지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 플레이리스트에는 트랙을 추가할 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/playlists/{id}/tracks")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> addPlaylistTrackList(
            @Parameter(description = "트랙을 추가할 플레이리스트 ID", example = "1", required = true) @PathVariable Long id,
            @Parameter(hidden = true)
            @LoginUserId Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "추가할 트랙 목록",
                    required = true,
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = PlaylistTrackCreateRequestDto.class)),
                            examples = @ExampleObject(
                                    name = "트랙 추가 예시",
                                    value = """
                                            [
                                              {
                                                "trackId": "5XeFesFbtLpXzIVDNQP22n",
                                                "title": "I Wanna Be Yours",
                                                "artistName": "Arctic Monkeys",
                                                "albumName": "AM",
                                                "albumImageUrl": "https://i.scdn.co/image/ab67616d0000b2734ae1c4c5c45aabe565499163",
                                                "spotifyUrl": "https://open.spotify.com/track/5XeFesFbtLpXzIVDNQP22n",
                                                "durationMs": 183956,
                                                "youtubeUrl": "https://www.youtube.com/watch?v=nyuo9-OjNNg"
                                              }
                                            ]
                                            """
                            )
                    )
            )
            @RequestBody List<PlaylistTrackCreateRequestDto> requestDtoList) {
        PlaylistValidator.validateRequestList(requestDtoList);

        PlaylistDetailResponseDto result = playlistService.addPlaylistTrackList(
                id, userId, PlaylistTrackConvert.toEntityList(requestDtoList, id));
        return ApiResponseDto.success(result, "트랙이 플레이리스트에 추가되었습니다.");
    }

    @Operation(
            summary = "플레이리스트 댓글 작성",
            description = "로그인한 사용자가 플레이리스트에 댓글을 작성합니다. 비공개 플레이리스트에는 소유자만 댓글을 작성할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 작성 성공", content = @Content(schema = @Schema(implementation = PlaylistDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 본문이 비어 있거나 댓글 내용이 올바르지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "액세스 토큰이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "플레이리스트 또는 사용자를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/playlists/{id}/comments")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> createPlaylistComment(
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
        PlaylistValidator.validateCommentRequestDto(requestDto);

        PlaylistDetailResponseDto result = playlistService.createPlaylistComment(id, userId, CommentConvert.toEntity(requestDto));
        return ApiResponseDto.success(result, "댓글이 추가되었습니다.");
    }

    @Operation(summary = "플레이리스트 좋아요/취소", description = "로그인한 사용자가 플레이리스트에 좋아요를 누르거나 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 상태 변경 성공", content = @Content(schema = @Schema(implementation = LikeResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/playlists/{id}/likes")
    @AccessTokenCheck
    public ApiResponseDto<LikeResponseDto> togglePlaylistLike(
            @Parameter(description = "좋아요 상태를 변경할 플레이리스트 ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(hidden = true)
            @LoginUserId Long userId) {
        LikeResponseDto likeResponseDto = likeService.togglePlaylistLike(id, userId);

        return ApiResponseDto.success(likeResponseDto, "플레이리스트 좋아요 상태 변경 성공");
    }

    @Operation(summary = "공개 목록 조회", description = "공개된 플레이리스트를 페이지 단위로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공개 플레이리스트 목록 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "페이지 또는 크기 값이 올바르지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/playlists")
    public ApiResponseDto<Map<String, Object>> getPublicPlaylistList(
            @Parameter(description = "페이지 번호", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "검색어", example = "로맨틱") @RequestParam(required = false) String keyword) {
        Map<String, Object> result = playlistService.getPublicPlaylistList(page, size, keyword);
        return ApiResponseDto.success(result, "공개 플레이리스트 목록 조회 성공");
    }

    @Operation(summary = "내 플레이리스트 목록", description = "로그인한 사용자의 전체 플레이리스트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 플레이리스트 목록 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/users/me/playlists")
    @AccessTokenCheck
    public ApiResponseDto<List<PlaylistResponseDto>> getMyPlaylistList(
            @Parameter(hidden = true) @LoginUserId Long userId) {
        List<PlaylistResponseDto> playlistResponseDtoList = playlistService.getMyPlaylistList(userId);
        return ApiResponseDto.success(playlistResponseDtoList, "조회 성공");
    }

    @Operation(summary = "인기 플레이리스트 랭킹", description = "like(좋아요 수) 또는 view(조회수) 기준으로 랭킹을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기 플레이리스트 랭킹 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "랭킹 기준 또는 조회 개수가 올바르지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/playlists/ranking")
    public ApiResponseDto<List<PlaylistResponseDto>> getPlaylistRanking(
            @Parameter(description = "조회할 랭킹 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "랭킹 기준. like 또는 view", example = "like")
            @RequestParam(defaultValue = "like") String type) {
        List<PlaylistResponseDto> playlistResponseDtoList = playlistService.getPlaylistRanking(limit, type);
        return ApiResponseDto.success(playlistResponseDtoList, "인기 플레이리스트 랭킹 조회 성공");
    }

    @Operation(summary = "플레이리스트 상세 조회", description = "플레이리스트 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플레이리스트 상세 조회 성공", content = @Content(schema = @Schema(implementation = PlaylistDetailResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/playlists/{id}")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> getPlaylistDetail(
            @Parameter(description = "조회할 플레이리스트 ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(hidden = true)
            @LoginUserId Long userId) {
        PlaylistDetailResponseDto result = playlistService.getPlaylistDetail(id, userId);
        return ApiResponseDto.success(result, "조회 성공");
    }

    @Operation(
            summary = "플레이리스트 수정",
            description = "로그인한 사용자가 본인 소유 플레이리스트를 수정합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = PlaylistMultipartRequestDto.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플레이리스트 수정 성공", content = @Content(schema = @Schema(implementation = PlaylistDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 플레이리스트는 수정할 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @AccessTokenCheck
    @PutMapping(value = "/playlists/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<PlaylistDetailResponseDto> updatePlaylist(
            @Parameter(description = "수정할 플레이리스트 ID", example = "1", required = true)
            @PathVariable("id") Long playlistId,
            @Valid @ModelAttribute PlaylistRequestDto request,
            @Parameter(
                    description = "플레이리스트 커버 이미지",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @Parameter(hidden = true)
            @LoginUserId Long userId) {
        PlaylistDetailResponseDto result = playlistService.updatePlaylist(
                playlistId, userId, PlaylistConvert.toEntity(request), coverImage);
        return ApiResponseDto.success(result, "플레이리스트가 수정되었습니다.");
    }

    @Operation(summary = "공개/비공개 설정", description = "로그인한 사용자가 본인 소유 플레이리스트의 공개 여부를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공개 여부 변경 성공", content = @Content(schema = @Schema(implementation = PlaylistDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "공개 여부 값은 Y 또는 N이어야 합니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 플레이리스트는 변경할 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/playlists/{id}/visibility")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> updatePlaylistVisibility(
            @Parameter(description = "공개 여부를 변경할 플레이리스트 ID", example = "1", required = true)
            @PathVariable("id") Long playlistId,
            @Parameter(description = "공개 여부", example = "Y", required = true)
            @RequestParam String publicYn,
            @Parameter(hidden = true) @LoginUserId Long userId) {
        PlaylistDetailResponseDto result = playlistService.updatePlaylistVisibility(
                playlistId, userId, publicYn);
        return ApiResponseDto.success(result, "플레이리스트 공개 여부가 변경되었습니다.");
    }

    @Operation(summary = "플레이리스트 트랙 순서 변경", description = "프론트에서 드래그앤드랍 후 전달한 트랙 목록의 배열 순서대로 POSITION_NO를 1부터 다시 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "트랙 순서 변경 성공", content = @Content(schema = @Schema(implementation = PlaylistDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청한 트랙 순서 목록이 올바르지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 플레이리스트의 트랙 순서는 변경할 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "플레이리스트 또는 트랙을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/playlists/{id}/tracks/reorder")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> updatePlaylistTrackOrder(
            @Parameter(description = "트랙 순서를 변경할 플레이리스트 ID", example = "1", required = true) @PathVariable Long id,
            @Parameter(hidden = true)
            @LoginUserId Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "드래그앤드랍 후 새 순서대로 정렬된 플레이리스트 트랙 목록",
                    required = true,
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = PlaylistTrackReorderRequestDto.class)),
                            examples = @ExampleObject(
                                    name = "트랙 순서 변경 예시",
                                    value = """
                                            [
                                              {
                                                "playlistTrackId": 10,
                                                "positionNo": 1
                                              },
                                              {
                                                "playlistTrackId": 11,
                                                "positionNo": 2
                                              }
                                            ]
                                            """
                            )
                    )
            )
            @RequestBody List<PlaylistTrackReorderRequestDto> requestDtoList) {
        PlaylistValidator.validateRequestList(requestDtoList);

        PlaylistDetailResponseDto result = playlistService.updatePlaylistTrackOrder(
                id, userId, PlaylistTrackConvert.toReorderEntityList(requestDtoList));
        return ApiResponseDto.success(result, "트랙 순서가 변경되었습니다.");
    }

    @Operation(
            summary = "플레이리스트 댓글 수정",
            description = "로그인한 사용자가 본인이 작성한 플레이리스트 댓글 내용을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공", content = @Content(schema = @Schema(implementation = PlaylistDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 본문이 비어 있거나 댓글 내용이 올바르지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "액세스 토큰이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/playlists/{id}/comments/{commentId}")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> updatePlaylistComment(
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
            @LoginUserId Long userId
    ) {
        PlaylistValidator.validateCommentRequestDto(requestDto);

        PlaylistDetailResponseDto result = playlistService.updatePlaylistComment(id, commentId, userId, CommentConvert.toEntity(requestDto));
        return ApiResponseDto.success(result, "댓글이 수정되었습니다.");
    }

    @Operation(summary = "플레이리스트 삭제", description = "로그인한 사용자가 본인 소유 플레이리스트를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플레이리스트 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 플레이리스트는 삭제할 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @AccessTokenCheck
    @DeleteMapping("/playlists/{id}")
    public ApiResponseDto<Void> deletePlaylist(
            @Parameter(description = "삭제할 플레이리스트 ID", example = "1", required = true)
            @PathVariable("id") Long playlistId,
            @Parameter(hidden = true)
            @LoginUserId Long userId) {
        playlistService.deletePlaylist(playlistId, userId);
        return ApiResponseDto.success(null, "플레이리스트가 삭제되었습니다.");
    }

    @Operation(summary = "플레이리스트 트랙 삭제", description = "로그인한 사용자가 본인 소유 플레이리스트에서 특정 트랙을 제거합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "트랙 삭제 성공", content = @Content(schema = @Schema(implementation = PlaylistDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청한 트랙 ID 형식이 올바르지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 플레이리스트의 트랙은 삭제할 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "플레이리스트 또는 트랙을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/playlists/{id}/tracks/{trackId}")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> deletePlaylistTrack(
            @Parameter(hidden = true)
            @LoginUserId Long userId,
            @Parameter(description = "트랙을 삭제할 플레이리스트 ID", example = "1", required = true) @PathVariable Long id,
            @Parameter(description = "삭제할 플레이리스트 트랙 ID", example = "10", required = true) @PathVariable Long trackId) {
        PlaylistDetailResponseDto result = playlistService.deletePlaylistTrack(id, userId, trackId);
        return ApiResponseDto.success(result, "트랙이 플레이리스트에서 제거되었습니다.");
    }

    @Operation(
            summary = "플레이리스트 댓글 삭제",
            description = "로그인한 사용자가 본인이 작성한 플레이리스트 댓글을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공", content = @Content(schema = @Schema(implementation = PlaylistDetailResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "액세스 토큰이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/playlists/{id}/comments/{commentId}")
    @AccessTokenCheck
    public ApiResponseDto<PlaylistDetailResponseDto> deletePlaylistComment(
            @Parameter(description = "댓글이 속한 플레이리스트 ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "삭제할 댓글 ID", example = "10", required = true)
            @PathVariable Long commentId,
            @Parameter(hidden = true)
            @LoginUserId Long userId) {
        PlaylistDetailResponseDto result = playlistService.deletePlaylistComment(id, commentId, userId);
        return ApiResponseDto.success(result, "댓글이 삭제되었습니다.");
    }
}
