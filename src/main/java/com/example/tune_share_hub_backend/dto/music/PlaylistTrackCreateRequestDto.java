package com.example.tune_share_hub_backend.dto.music;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistTrackCreateRequestDto {
	private String trackId;
	private String title;
	private String artistName;
	private String albumName;
	private String albumImageUrl;
	private String spotifyUrl;
	private Long durationMs;
	@Setter
	private String youtubeUrl;

}
