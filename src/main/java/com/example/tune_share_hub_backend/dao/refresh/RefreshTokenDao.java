package com.example.tune_share_hub_backend.dao.refresh;

import com.example.tune_share_hub_backend.entity.refresh.Refresh;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenDao {
    public void insert(Refresh refresh);
    public int existsRefresh(String refreshToken);

}
