package com.example.tune_share_hub_backend.convert;

import java.util.ArrayList;
import java.util.List;

import com.example.tune_share_hub_backend.dto.spotify.MusicSearchResponseDto;
import com.example.tune_share_hub_backend.dto.spotify.SpotifyTrackSearchResponseDto;

public class SpotifyTrackConverter {

	private SpotifyTrackConverter() {
	}

	public static List<MusicSearchResponseDto> toMusicSearchResponseDtos(
		SpotifyTrackSearchResponseDto response
	) {
		List<MusicSearchResponseDto> result = new ArrayList<>();

		if (response == null || response.getTracks() == null) {
			return result;
		}

		List<SpotifyTrackSearchResponseDto.TrackItem> items =
			response.getTracks().getItems();

		if (items == null || items.isEmpty()) {
			return result;
		}

		for (SpotifyTrackSearchResponseDto.TrackItem item : items) {
			result.add(toMusicSearchResponseDto(item));
		}

		return result;
	}

	private static MusicSearchResponseDto toMusicSearchResponseDto(
		SpotifyTrackSearchResponseDto.TrackItem item
	) {
		String albumName = "";
		String albumImageUrl = "";

		if (item.getAlbum() != null) {
			albumName = item.getAlbum().getName();
			albumImageUrl = item.getAlbum().getFirstImageUrl();
		}

		return new MusicSearchResponseDto(
			item.getId(),
			item.getName(),
			item.getFirstArtistName(),
			albumName,
			albumImageUrl,
			item.getSpotifyUrl(),
			item.getPreviewUrl(),
			item.getDurationMs()
		);
	}
}
