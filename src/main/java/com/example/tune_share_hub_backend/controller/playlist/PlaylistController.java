package com.example.tune_share_hub_backend.controller.playlist;

import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistUpdateRequestDto;
import com.example.tune_share_hub_backend.exception.ApiException;
import com.example.tune_share_hub_backend.interceptor.AccessTokenCheck;
import com.example.tune_share_hub_backend.service.playlist.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
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
    // @AccessTokenCheck가 붙은 메서드는 인터셉터에서 JWT 검증을 먼저 수행한다.
    @AccessTokenCheck
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> updatePlaylist(
            // URL의 {id} 값을 playlistId 매개변수로 받는다.
            @PathVariable("id") Long playlistId,
            HttpServletRequest httpServletRequest,
            // 요청 JSON body를 PlaylistUpdateRequestDto 객체로 변환해서 받는다.
            @RequestBody PlaylistUpdateRequestDto request
    ) {
        Long userId = getLoginUserId(httpServletRequest);
        playlistService.updatePlaylist(playlistId, userId, request);
        return ResponseEntity.ok(ApiResponseDto.success(null, "플레이리스트가 수정되었습니다."));
    }

    private Long getLoginUserId(HttpServletRequest request) {
        // AccessTokenCheckInterceptor가 JWT에서 꺼낸 userId를 request attribute에 저장해둔다.
        Object userId = request.getAttribute("userId");
        if (userId instanceof Long value) {
            return value;
        }
        if (userId instanceof Number value) {
            return value.longValue();
        }
        if (userId instanceof String value) {
            return Long.valueOf(value);
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인 사용자 정보를 찾을 수 없습니다.");
    }
}
