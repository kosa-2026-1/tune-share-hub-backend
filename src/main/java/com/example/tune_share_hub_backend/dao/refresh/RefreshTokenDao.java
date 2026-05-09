package com.example.tune_share_hub_backend.dao.refresh;

import com.example.tune_share_hub_backend.entity.refresh.Refresh;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenDao {
    void insert(Refresh refresh);
    int deleteByUserIdAndTokenValue(Long userId, String refreshToken);
    int existsRefresh(String refreshToken, Long userId);
    int revokeTokensByUserIdAndTokenValue(Long userId, String refreshToken);
    int deleteExpiredOrRevokedTokens();
}
