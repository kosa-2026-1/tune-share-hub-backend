package com.example.tune_share_hub_backend.dao.playlist;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.tune_share_hub_backend.entity.PlaylistTrack;

@Mapper
public interface PlaylistTrackDao {
	void insertPlaylistTrack(@Param("track") PlaylistTrack track);
	List<PlaylistTrack> findByPlaylistId(@Param("playlistId") Long playlistId);
	int deletePlaylistTrack(@Param("trackId") Long trackId);
	int updatePlaylistTrackPosition(@Param("trackId") Long trackId, @Param("positionNo") int positionNo);
}
