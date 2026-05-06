package com.example.tune_share_hub_backend.service.music;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.tune_share_hub_backend.client.SpotifyClient;
import com.example.tune_share_hub_backend.dto.music.MusicSearchResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MusicService {

	private final SpotifyClient spotifyClient;

	public List<MusicSearchResponseDto> searchMusic(String keyword) {
		validateKeyword(keyword);
		return spotifyClient.searchTracks(keyword);
	}

	private void validateKeyword(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			throw new IllegalArgumentException("검색어는 필수입니다.");
		}
	}
}
