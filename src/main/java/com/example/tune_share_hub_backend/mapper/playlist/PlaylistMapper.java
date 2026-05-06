package com.example.tune_share_hub_backend.mapper.playlist;

import com.example.tune_share_hub_backend.dto.playlist.PlaylistUpdateRequestDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlaylistMapper {
    // 수정 성공 시 1, 조건에 맞는 플레이리스트가 없으면 0을 반환한다.
    int updatePlaylist(@Param("playlistId") Long playlistId,
                       @Param("userId") Long userId,
                       @Param("request") PlaylistUpdateRequestDto request);
}
