package com.example.tune_share_hub_backend.controller.playlist;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tune_share_hub_backend.dto.PlaylistRequestDto;
import com.example.tune_share_hub_backend.dto.PlaylistResponseDto;
import com.example.tune_share_hub_backend.service.playlist.PlaylistService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlaylistController {


    private final PlaylistService playlistService;

    // TODO: 로그인 파트 합쳐지면 교체
    private Long getCurrentUserId() {
        return 1L; // 임시 하드코딩(user에 1, test로 넣어놓음.)
    }

    // MVP-4: 생성
    @PostMapping("/playlists")
    public ResponseEntity<?> create(@RequestBody @Valid PlaylistRequestDto req) {
        PlaylistResponseDto result = playlistService.create(getCurrentUserId(), req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result,
                "message", "플레이리스트 생성 성공"
        ));
    }

    // MVP-5: 내 목록 조회
    @GetMapping("/users/me/playlists")
    public ResponseEntity<?> getMyPlaylists() {
        List<PlaylistResponseDto> result = playlistService.getMyPlaylists(getCurrentUserId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result,
                "message", "조회 성공"
        ));
    }

    // MVP-6: 상세 조회
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
