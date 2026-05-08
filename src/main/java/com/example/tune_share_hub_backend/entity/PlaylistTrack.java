package com.example.tune_share_hub_backend.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistTrack {
	private Long playlistTrackId;
	private Long playlistId;
	private String spotifyTrackId;
	private String title;
	private String artistName;
	private String albumName;
	private String albumImageUrl;
	private String spotifyUrl;
	private String youtubeUrl;
	private Long durationMs;
	private Integer positionNo;
	private LocalDateTime createdAt;

}
