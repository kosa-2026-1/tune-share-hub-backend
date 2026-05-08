package com.example.tune_share_hub_backend.dto.music;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PlaylistTrackResponseDto {
	private String trackId;
	private String title;
	private String artistName;
	private String albumName;
	private String albumImageUrl;
	private String spotifyUrl;
	private String youtubeUrl;
	private int positionNo;
}
