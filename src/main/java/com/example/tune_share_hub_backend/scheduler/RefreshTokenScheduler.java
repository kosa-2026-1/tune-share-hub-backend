package com.example.tune_share_hub_backend.scheduler;

import com.example.tune_share_hub_backend.dao.refresh.RefreshTokenDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenScheduler {
    private final RefreshTokenDao refreshTokenDao;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시에 실행
    @Transactional
    public void deleteExpiredOrRevokedTokens() {
        int deletedCount = refreshTokenDao.deleteExpiredOrRevokedTokens();
        log.info("Deleted {} expired or revoked refresh tokens", deletedCount);
    }
}
