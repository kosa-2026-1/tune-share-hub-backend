package com.example.tune_share_hub_backend.service.playlist;

import com.example.tune_share_hub_backend.dto.playlist.PlaylistVisibilityUpdateRequestDto;
import com.example.tune_share_hub_backend.dto.playlist.PlaylistUpdateRequestDto;
import com.example.tune_share_hub_backend.exception.ApiException;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaylistService {
    // MyBatis XML에 정의된 SQL을 호출하는 DAO이다.
    private final PlaylistDao playlistDao;

    public PlaylistService(PlaylistDao playlistDao) {
        this.playlistDao = playlistDao;
    }

    @Transactional
    public void updatePlaylist(Long playlistId, Long userId, PlaylistUpdateRequestDto request) {
        // 요청값 검증을 먼저 끝낸 뒤 DB update를 시도한다.
        validatePlaylistId(playlistId);
        validateRequest(request);

        // UPDATE 조건에 playlistId와 userId가 함께 들어가므로, 본인 소유 플레이리스트만 수정된다.
        int updatedCount = playlistDao.updatePlaylist(playlistId, userId, request);
        if (updatedCount == 0) {
            // 수정된 행이 없으면 존재하지 않거나, 삭제되었거나, 다른 사용자의 플레이리스트로 판단한다.
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "PLAYLIST_UPDATE_FORBIDDEN",
                    "플레이리스트를 수정할 수 없습니다."
            );
        }
    }

    @Transactional
    public void updatePlaylistVisibility(Long playlistId, Long userId, PlaylistVisibilityUpdateRequestDto request) {
        // 공개 여부 변경 API는 publicYn만 검증하면 된다.
        validatePlaylistId(playlistId);
        validateVisibilityRequest(request);

        // 공개 여부만 바꾸는 전용 API이지만, 본인 소유 검증 조건은 플레이리스트 수정과 동일하다.
        int updatedCount = playlistDao.updatePlaylistVisibility(playlistId, userId, request.getPublicYn());
        if (updatedCount == 0) {
            // 수정된 행이 없으면 존재하지 않거나, 삭제되었거나, 다른 사용자의 플레이리스트로 판단한다.
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "PLAYLIST_VISIBILITY_UPDATE_FORBIDDEN",
                    "플레이리스트 공개 여부를 변경할 수 없습니다."
            );
        }
    }

    private void validatePlaylistId(Long playlistId) {
        // URL 경로로 받은 playlistId가 잘못된 값이면 DB에 접근하지 않는다.
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

        validatePublicYn(request.getPublicYn());
    }

    private void validateVisibilityRequest(PlaylistVisibilityUpdateRequestDto request) {
        // 공개 여부만 변경하더라도 요청 본문 자체는 반드시 필요하다.
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 본문이 필요합니다.");
        }

        validatePublicYn(request.getPublicYn());
    }

    private void validatePublicYn(String publicYn) {
        // DB 제약 조건과 동일하게 PUBLIC_YN은 Y 또는 N만 허용한다.
        if (!"Y".equals(publicYn) && !"N".equals(publicYn)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PUBLIC_YN", "공개 여부는 Y 또는 N만 가능합니다.");
        }
    }
}
