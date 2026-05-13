package com.example.tune_share_hub_backend.convert;

import java.util.ArrayList;
import java.util.List;

import com.example.tune_share_hub_backend.dto.music.MusicSearchResponseDto;
import com.example.tune_share_hub_backend.dto.music.SpotifyTrackSearchResponseDto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SpotifyTrackConverter {

	public static List<MusicSearchResponseDto> toMusicSearchResponseDtoList(
		SpotifyTrackSearchResponseDto response
	) {
		List<MusicSearchResponseDto> musicSearchResponseDtoList = new ArrayList<>();

		if (response == null || response.getTracks() == null) {
			return musicSearchResponseDtoList;
		}

		List<SpotifyTrackSearchResponseDto.TrackItem> trackItemList =
			response.getTracks().getItems();

		if (trackItemList == null || trackItemList.isEmpty()) {
			return musicSearchResponseDtoList;
		}

		for (SpotifyTrackSearchResponseDto.TrackItem item : trackItemList) {
			musicSearchResponseDtoList.add(toMusicSearchResponseDto(item));
		}

		return musicSearchResponseDtoList;
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
