package com.example.tune_share_hub_backend.dao.refresh;

import com.example.tune_share_hub_backend.entity.refresh.Refresh;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenDao {
    public void insert(Refresh refresh);
    public int deleteByUserIdAndTokenValue(@Param("userId") Long userId, @Param("refreshToken") String refreshToken);
    public int existsRefresh(@Param("refreshToken") String refreshToken, @Param("userId") Long userId);
    public int revokeTokensByUserIdAndTokenValue(@Param("userId") Long userId, @Param("refreshToken") String refreshToken);
}
