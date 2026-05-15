package com.example.tune_share_hub_backend.dao.playlist;

import com.example.tune_share_hub_backend.entity.PlaylistTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlaylistTrackDao {
    void insertPlaylistTrack(@Param("track") PlaylistTrack track);

    void insertPlaylistTracks(@Param("tracks") List<PlaylistTrack> tracks);

    List<PlaylistTrack> findByPlaylistId(@Param("playlistId") Long playlistId);

    int existsByPlaylistIdAndTrackId(@Param("playlistId") Long playlistId, @Param("trackId") Long trackId);

    int deletePlaylistTrack(@Param("trackId") Long trackId);

    int deleteByPlaylistId(@Param("playlistId") Long playlistId);

    int updatePlaylistTrackPosition(@Param("trackId") Long trackId, @Param("positionNo") int positionNo);

    void updatePlaylistTrackPositions(@Param("tracks") List<PlaylistTrack> tracks);

    void copyPlaylistTracks(@Param("originalPlaylistId") Long originalPlaylistId, @Param("newPlaylistId") Long newPlaylistId);

}