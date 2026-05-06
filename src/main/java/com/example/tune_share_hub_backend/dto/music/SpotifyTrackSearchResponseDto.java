package com.example.tune_share_hub_backend.dto.music;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SpotifyTrackSearchResponseDto {

	private Tracks tracks;

	@Getter
	@Setter
	@NoArgsConstructor
	public static class Tracks {

		private List<TrackItem> items;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class TrackItem {

		private String id;

		private String name;

		@JsonProperty("duration_ms")
		private Integer durationMs;

		@JsonProperty("preview_url")
		private String previewUrl;

		@JsonProperty("external_urls")
		private ExternalUrls externalUrls;

		private Album album;

		private List<Artist> artists;

		public String getFirstArtistName() {
			if (artists == null || artists.isEmpty()) {
				return "";
			}

			return artists.get(0).getName();
		}

		public String getSpotifyUrl() {
			if (externalUrls == null) {
				return "";
			}

			return externalUrls.getSpotify();
		}
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class ExternalUrls {

		private String spotify;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class Album {

		private String name;

		private List<Image> images;

		public String getFirstImageUrl() {
			if (images == null || images.isEmpty()) {
				return "";
			}

			return images.get(0).getUrl();
		}
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class Image {

		private String url;

		private Integer height;

		private Integer width;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class Artist {

		private String name;
	}
}
