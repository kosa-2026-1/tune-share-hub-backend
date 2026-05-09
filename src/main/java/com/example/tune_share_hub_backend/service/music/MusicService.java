package com.example.tune_share_hub_backend.service.music;

import com.example.tune_share_hub_backend.client.SpotifyClient;
import com.example.tune_share_hub_backend.client.YoutubeClient;
import com.example.tune_share_hub_backend.dto.music.MusicSearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final SpotifyClient spotifyClient;
    private final YoutubeClient youtubeClient;

    public List<MusicSearchResponseDto> searchMusic(String keyword) {
        validateKeyword(keyword);
        List<MusicSearchResponseDto> musicSearchResponseDtos = spotifyClient.searchTracks(keyword);
        youtubeClient.addVideoUrl(musicSearchResponseDtos);

        return musicSearchResponseDtos;
    }

    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 필수입니다.");
        }
    }

    public MusicSearchResponseDto getTrack(String trackId) {
        return spotifyClient.getTrack(trackId);
    }
}
