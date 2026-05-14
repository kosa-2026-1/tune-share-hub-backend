package com.example.tune_share_hub_backend.controller.user;

import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistResponseDto;
import com.example.tune_share_hub_backend.dto.user.UserResponseDto;
import com.example.tune_share_hub_backend.global.interceptor.AccessTokenCheck;
import com.example.tune_share_hub_backend.global.interceptor.LoginUserId;
import com.example.tune_share_hub_backend.service.like.LikeService;
import com.example.tune_share_hub_backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 API")
public class UserController {
    private final UserService userService;
    private final LikeService likeService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 정보 조회 성공", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.")
    })
    @AccessTokenCheck
    @GetMapping("/me")
    public ApiResponseDto<UserResponseDto> getUserInfo(
            @Parameter(hidden = true) @LoginUserId Long userId) {
        UserResponseDto userResponseDto = userService.getUserInfo(userId);
        return ApiResponseDto.success(userResponseDto, "내 정보 조회 성공");
    }

    @Operation(summary = "내가 좋아요한 플레이리스트 조회", description = "로그인한 사용자가 좋아요한 플레이리스트 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요한 플레이리스트 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.")
    })
    @AccessTokenCheck
    @GetMapping("/me/likes")
    public ApiResponseDto<List<PlaylistResponseDto>> getLikedPlaylistList(
            @Parameter(hidden = true) @LoginUserId Long userId) {
        List<PlaylistResponseDto> likedPlaylistList = likeService.getLikedPlaylistList(userId);
        return ApiResponseDto.success(likedPlaylistList, "좋아요한 플레이리스트 조회 성공");
    }
}
