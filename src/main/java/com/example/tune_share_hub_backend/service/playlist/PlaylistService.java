package com.example.tune_share_hub_backend.service.playlist;

import com.example.tune_share_hub_backend.dto.playlist.PlaylistUpdateRequestDto;
import com.example.tune_share_hub_backend.exception.ApiException;
import com.example.tune_share_hub_backend.mapper.playlist.PlaylistMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaylistService {
    private final PlaylistMapper playlistMapper;

    public PlaylistService(PlaylistMapper playlistMapper) {
        this.playlistMapper = playlistMapper;
    }

    @Transactional
    public void updatePlaylist(Long playlistId, Long userId, PlaylistUpdateRequestDto request) {
        validatePlaylistId(playlistId);
        validateRequest(request);

        // UPDATE 조건에 playlistId와 userId가 함께 들어가므로, 본인 소유 플레이리스트만 수정된다.
        int updatedCount = playlistMapper.updatePlaylist(playlistId, userId, request);
        if (updatedCount == 0) {
            // 수정된 행이 없으면 존재하지 않거나, 삭제되었거나, 다른 사용자의 플레이리스트로 판단한다.
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "PLAYLIST_UPDATE_FORBIDDEN",
                    "플레이리스트를 수정할 수 없습니다."
            );
        }
    }

    private void validatePlaylistId(Long playlistId) {
        if (playlistId == null || playlistId <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PLAYLIST_ID", "유효하지 않은 플레이리스트 ID입니다.");
        }
    }

    private void validateRequest(PlaylistUpdateRequestDto request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 본문이 필요합니다.");
        }

        // PLAYLISTS.TITLE은 NOT NULL이므로 빈 제목은 미리 차단한다.
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PLAYLIST_TITLE", "플레이리스트 제목은 필수입니다.");
        }

        // DB 제약 조건과 동일하게 PUBLIC_YN은 Y 또는 N만 허용한다.
        if (!"Y".equals(request.getPublicYn()) && !"N".equals(request.getPublicYn())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PUBLIC_YN", "공개 여부는 Y 또는 N만 가능합니다.");
        }
    }
}
