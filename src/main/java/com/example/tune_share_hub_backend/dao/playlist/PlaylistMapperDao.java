package com.example.tune_share_hub_backend.dao.playlist;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.tune_share_hub_backend.dto.playlist.PlaylistRequestDto;
import com.example.tune_share_hub_backend.entity.Playlist;

@Mapper
public interface PlaylistMapperDao {

    void insert(Playlist playlist);

    List<Playlist> findByUserId(Long userId);

    Playlist findById(Long playlistId);

    int updatePlaylist(@Param("playlistId") Long playlistId,
            @Param("userId") Long userId,
            @Param("request") PlaylistRequestDto request);

    int updatePlaylistVisibility(@Param("playlistId") Long playlistId,
            @Param("userId") Long userId,
            @Param("publicYn") String publicYn);

    List<Playlist> findPublicPlaylists(@Param("offset") int offset, @Param("size") int size);

    int countPublicPlaylists();

    int deletePlaylist(@Param("playlistId") Long playlistId,
            @Param("userId") Long userId);
}