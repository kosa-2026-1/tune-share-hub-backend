package com.example.tune_share_hub_backend.dao.playlist;

import com.example.tune_share_hub_backend.entity.Playlist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlaylistDao {

    void insert(Playlist playlist);

    List<Playlist> findByUserId(Long userId);

    Playlist findById(Long playlistId);

    int updatePlaylist(@Param("playlistId") Long playlistId,
                       @Param("userId") Long userId,
                       @Param("playlist") Playlist playlist);

    int updatePlaylistVisibility(@Param("playlistId") Long playlistId,
                                 @Param("userId") Long userId,
                                 @Param("publicYn") String publicYn);

    int increaseCommentCount(@Param("playlistId") Long playlistId);

    void decreaseCommentCount(@Param("playlistId") Long playlistId);

    List<Playlist> findPublicPlaylists(@Param("offset") int offset, @Param("size") int size);

    int countPublicPlaylists();

    int deletePlaylist(@Param("playlistId") Long playlistId,
                       @Param("userId") Long userId);

    List<Playlist> findTopPlaylists(@Param("limit") int limit, @Param("type") String type);

    void increaseViewCount(@Param("playlistId") Long playlistId);

    void increaseTrackCount(@Param("playlistId") Long playlistId, int size);

    void decreaseTrackCount(@Param("playlistId") Long playlistId);


}
