package com.example.tune_share_hub_backend.service.auth;

import com.example.tune_share_hub_backend.dao.refresh.RefreshTokenDao;
import com.example.tune_share_hub_backend.entity.refresh.Refresh;
import com.example.tune_share_hub_backend.global.config.security.JwtProvider;
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
}
