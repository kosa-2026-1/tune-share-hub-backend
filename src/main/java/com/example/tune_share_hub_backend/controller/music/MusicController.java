package com.example.tune_share_hub_backend.controller.music;

import com.example.tune_share_hub_backend.dto.music.MusicSearchResponseDto;
import com.example.tune_share_hub_backend.service.music.MusicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @Operation(
            summary = "Spotify 곡 검색",
            description = "검색어를 기준으로 Spotify에서 곡 정보를 검색합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "곡 검색 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "검색어가 비어 있거나 잘못된 요청입니다."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Spotify API 호출 중 서버 오류가 발생했습니다."
            )
    })
    @GetMapping("/search")
    public ResponseEntity<List<MusicSearchResponseDto>> searchMusic(
            @Parameter(
                    description = "검색할 곡명 또는 아티스트명",
                    example = "아이유",
                    required = true
            )
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(musicService.searchMusic(keyword));
    }

    @Operation(summary = "트랙 상세 정보 조회", description = "Spotify Track ID로 트랙 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "트랙 조회 성공"),
            @ApiResponse(responseCode = "500", description = "Spotify API 호출 중 서버 오류가 발생했습니다.")
    })
    @GetMapping("/tracks/{id}")
    public ResponseEntity<MusicSearchResponseDto> getTrack(
            @Parameter(description = "Spotify Track ID", example = "3n3Ppam7vgaVa1iaRUIOKE", required = true)
            @PathVariable String id) {
        return ResponseEntity.ok(musicService.getTrack(id));
    }

}
