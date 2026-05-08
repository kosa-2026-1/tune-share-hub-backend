package com.example.tune_share_hub_backend.convert;

import java.util.ArrayList;
import java.util.List;


import com.example.tune_share_hub_backend.dto.music.PlaylistTrackCreateRequestDto;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackResponseDto;
import com.example.tune_share_hub_backend.entity.PlaylistTrack;

public class PlaylistTrackConvert {

	private PlaylistTrackConvert() {
	}

	public static PlaylistTrack toEntity(
		PlaylistTrackCreateRequestDto request,
		Long playlistId,
		int positionNo
	) {
		return PlaylistTrack.builder()
			.playlistId(playlistId)
			.spotifyTrackId(request.getTrackId())
			.title(request.getTitle())
			.artistName(request.getArtistName())
			.albumName(request.getAlbumName())
			.albumImageUrl(request.getAlbumImageUrl())
			.spotifyUrl(request.getSpotifyUrl())
			.youtubeUrl(request.getYoutubeUrl())
			.durationMs(request.getDurationMs())
			.positionNo(positionNo)
			.build();
	}

	public static List<PlaylistTrack> toEntities(
		List<PlaylistTrackCreateRequestDto> requests,
		Long playlistId
	) {
		List<PlaylistTrack> playlistTracks = new ArrayList<>();

		for (int i = 0; i < requests.size(); i++) {
			PlaylistTrack playlistTrack = toEntity(
				requests.get(i),
				playlistId,
				i + 1
			);

			playlistTracks.add(playlistTrack);
		}

		return playlistTracks;
	}

	public static PlaylistTrackResponseDto toResponseDto(PlaylistTrack playlistTrack) {
		return PlaylistTrackResponseDto.builder()
			.trackId(playlistTrack.getSpotifyTrackId())
			.title(playlistTrack.getTitle())
			.artistName(playlistTrack.getArtistName())
			.albumName(playlistTrack.getAlbumName())
			.albumImageUrl(playlistTrack.getAlbumImageUrl())
			.spotifyUrl(playlistTrack.getSpotifyUrl())
			.youtubeUrl(playlistTrack.getYoutubeUrl())
			.durationMs(playlistTrack.getDurationMs())
			.positionNo(playlistTrack.getPositionNo())
			.build();
	}

	public static List<PlaylistTrackResponseDto> toResponseDtoList(
		List<PlaylistTrack> playlistTracks
	) {
		List<PlaylistTrackResponseDto> responseDtos = new ArrayList<>();

		for (PlaylistTrack playlistTrack : playlistTracks) {
			responseDtos.add(toResponseDto(playlistTrack));
		}

		return responseDtos;
	}
}
