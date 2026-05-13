package com.example.tune_share_hub_backend.service.music;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.tune_share_hub_backend.client.SpotifyClient;
import com.example.tune_share_hub_backend.client.YoutubeClient;
import com.example.tune_share_hub_backend.dto.music.MusicSearchResponseDto;
import com.example.tune_share_hub_backend.validate.MusicValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MusicService {

	private final SpotifyClient spotifyClient;
	private final YoutubeClient youtubeClient;

	public List<MusicSearchResponseDto> searchMusic(String keyword) {
		MusicValidator.validateKeyword(keyword);
		List<MusicSearchResponseDto> musicSearchResponseDtoList = spotifyClient.searchTracks(keyword);
		youtubeClient.addVideoUrl(musicSearchResponseDtoList);

		return musicSearchResponseDtoList;
	}

}
