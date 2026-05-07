package com.example.tune_share_hub_backend.service.auth;

import com.example.tune_share_hub_backend.dao.refresh.RefreshTokenDao;
import com.example.tune_share_hub_backend.entity.refresh.Refresh;
import com.example.tune_share_hub_backend.global.config.security.JwtProvider;
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
    private final JwtProvider jwtProvider;

    @Transactional
    public void saveRefreshToken(Long userId, String refreshTokenValue){
        Refresh refresh = Refresh.builder()
                .userId(userId)
                .tokenValue(refreshTokenValue)
                .revokedYn(false) // 초기에는 유효
                .expiresAt(jwtProvider.getExpirationDateTime(refreshTokenValue))
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenDao.insert(refresh);
    }

    @Transactional
    public void rotateToken(Long userId, String oldRefreshTokenValue, String newRefreshTokenValue) {
        // 입력 검증
        if (userId == null || oldRefreshTokenValue == null || newRefreshTokenValue == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "사용자 ID와 토큰 값은 필수입니다.");
        }

        // 기존 토큰 물리 삭제
        int deletedCount = refreshTokenDao.deleteByUserIdAndTokenValue(userId, oldRefreshTokenValue);

        if (deletedCount == 0) {
            // 삭제된 게 없다면 이미 탈취되어 사용됐거나 잘못된 토큰임
            log.warn("Refresh token reuse detected or invalid token for userId: {}", userId);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        
        log.info("Successfully deleted {} refresh token(s) for userId: {}", deletedCount, userId);

        // 새 토큰 저장
        try {
            saveRefreshToken(userId, newRefreshTokenValue);
            log.info("Successfully saved new refresh token for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to save new refresh token for userId: {}, error: {}", userId, e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Transactional
    public void revokeToken(Long userId, String refreshTokenValue) {
        int revokedCount = refreshTokenDao.revokeTokensByUserIdAndTokenValue(userId, refreshTokenValue);

        if(revokedCount == 0) {
            log.warn("Refresh token reuse detected or invalid token for userId: {}", userId);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        log.info("Successfully revoked {} refresh token(s) for userId: {}", revokedCount, userId);
    }
}
