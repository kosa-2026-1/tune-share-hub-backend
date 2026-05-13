package com.example.tune_share_hub_backend.convert;

import java.util.ArrayList;
import java.util.List;


import com.example.tune_share_hub_backend.dto.music.PlaylistTrackCreateRequestDto;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackReorderRequestDto;
import com.example.tune_share_hub_backend.dto.music.PlaylistTrackResponseDto;
import com.example.tune_share_hub_backend.entity.PlaylistTrack;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PlaylistTrackConvert {

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

	public static List<PlaylistTrack> toEntityList(
		List<PlaylistTrackCreateRequestDto> requestDtoList,
		Long playlistId
	) {
		List<PlaylistTrack> playlistTrackList = new ArrayList<>();

		for (int i = 0; i < requestDtoList.size(); i++) {
			PlaylistTrack playlistTrack = toEntity(
				requestDtoList.get(i),
				playlistId,
				i + 1
			);

			playlistTrackList.add(playlistTrack);
		}

		return playlistTrackList;
	}

	public static List<PlaylistTrack> toReorderEntityList(List<PlaylistTrackReorderRequestDto> requestDtoList) {
		List<PlaylistTrack> playlistTrackList = new ArrayList<>();

		for (int i = 0; i < requestDtoList.size(); i++) {
			playlistTrackList.add(toPositionEntity(requestDtoList.get(i).getPlaylistTrackId(), i + 1));
		}

		return playlistTrackList;
	}

	public static PlaylistTrackResponseDto toResponseDto(PlaylistTrack playlistTrack) {
		return PlaylistTrackResponseDto.builder()
			.playlistTrackId(playlistTrack.getPlaylistTrackId())
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

	public static PlaylistTrack toPositionEntity(Long playlistTrackId, Integer positionNo) {
		return PlaylistTrack.builder()
			.playlistTrackId(playlistTrackId)
			.positionNo(positionNo)
			.build();
	}

	public static List<PlaylistTrackResponseDto> toResponseDtoList(
		List<PlaylistTrack> playlistTrackList
	) {
		List<PlaylistTrackResponseDto> responseDtoList = new ArrayList<>();

		for (PlaylistTrack playlistTrack : playlistTrackList) {
			responseDtoList.add(toResponseDto(playlistTrack));
		}

		return responseDtoList;
	}
}
