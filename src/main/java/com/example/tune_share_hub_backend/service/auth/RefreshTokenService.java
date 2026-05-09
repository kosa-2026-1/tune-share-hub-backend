package com.example.tune_share_hub_backend.service.auth;

import com.example.tune_share_hub_backend.dao.refresh.RefreshTokenDao;
import com.example.tune_share_hub_backend.entity.refresh.Refresh;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenDao refreshTokenDao;

    @Transactional
    public void saveRefreshToken(
            Long userId,
            String tokenValue,
            LocalDateTime expiresAt
    ) {

        Refresh refresh = Refresh.builder()
                .userId(userId)
                .tokenValue(tokenValue)
                .revokedYn(false)
                .expiresAt(expiresAt)
                .build();

        refreshTokenDao.insert(refresh);
    }

    public void validateRefreshTokenExists(String token, Long userId) {

        boolean exists = refreshTokenDao.existsRefresh(token, userId) > 0;

        if (!exists) {
            log.warn("Refresh token reuse detected. userId={}", userId);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }
    }

    @Transactional
    public void rotateToken(
            Long userId,
            String oldToken,
            String newToken,
            LocalDateTime expiresAt
    ) {

        if (userId == null || oldToken == null || newToken == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 기존 토큰 삭제
        int deletedCount = refreshTokenDao.deleteByUserIdAndTokenValue(userId, oldToken);

        if (deletedCount == 0) {
            log.warn("Refresh token reuse detected. userId={}", userId);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }

        // 새 토큰 저장
        saveRefreshToken(userId, newToken, expiresAt);
        log.info("Refresh token rotated for userId={}", userId);
    }

    @Transactional
    public void revokeToken(Long userId, String token) {

        int revoked = refreshTokenDao.revokeTokensByUserIdAndTokenValue(userId, token);

        if (revoked == 0) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        log.info("Refresh token revoked. userId={}", userId);
    }
}
