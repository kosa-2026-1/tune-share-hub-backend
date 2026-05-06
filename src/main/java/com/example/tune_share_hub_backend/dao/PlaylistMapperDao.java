package com.example.tune_share_hub_backend.dao;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.tune_share_hub_backend.entity.Playlist;

@Mapper

public interface PlaylistMapperDao {
    void insert(Playlist playlist);
    List<Playlist> findByUserId(Long userId);
    Playlist findById(Long playlistId);
}
