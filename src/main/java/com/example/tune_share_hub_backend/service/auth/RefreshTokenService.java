package com.example.tune_share_hub_backend.service.auth;

import com.example.tune_share_hub_backend.dao.refresh.RefreshTokenDao;
import com.example.tune_share_hub_backend.entity.refresh.Refresh;
import com.example.tune_share_hub_backend.validate.AuthValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    public void checkRefreshTokenExists(String token, Long userId) {
        boolean exists = refreshTokenDao.existsRefresh(token, userId) > 0;
        AuthValidator.validateRefreshTokenExists(exists);
    }

    @Transactional
    public void rotateToken(
            Long userId,
            String oldToken,
            String newToken,
            LocalDateTime expiresAt
    ) {

        AuthValidator.validateRotateTokenRequest(userId, oldToken, newToken);
        int deletedCount = refreshTokenDao.deleteByUserIdAndTokenValue(userId, oldToken);
        AuthValidator.validateRefreshTokenDeleted(deletedCount);
        saveRefreshToken(userId, newToken, expiresAt);
    }

    @Transactional
    public void revokeToken(Long userId, String token) {
        int revoked = refreshTokenDao.revokeTokensByUserIdAndTokenValue(userId, token);
        AuthValidator.validateRefreshTokenRevoked(revoked);
    }
}
