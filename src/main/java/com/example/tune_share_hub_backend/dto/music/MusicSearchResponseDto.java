package com.example.tune_share_hub_backend.dto.music;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class MusicSearchResponseDto {
	private String trackId;
	private String title;
	private String artistName;
	private String albumName;
	private String albumImageUrl;
	private String spotifyUrl;
	@Setter
	private String youtubeUrl;
	private Integer durationMs;

}
