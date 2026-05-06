package com.example.tune_share_hub_backend.dto.music;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MusicSearchResponseDto {
	private String trackId;
	private String title;
	private String artistName;
	private String albumName;
	private String albumImageUrl;
	private String spotifyUrl;
	private String previewUrl;
	private Integer durationMs;

}
