package com.example.tune_share_hub_backend.dto.music;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class YoutubeSearchResponseDto {

	private List<YoutubeItemDto> items;

	@Getter
	@Setter
	@NoArgsConstructor
	public static class YoutubeItemDto {
		private YoutubeIdDto id;
		private YoutubeSnippetDto snippet;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class YoutubeIdDto {
		private String videoId;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class YoutubeSnippetDto {
		private String title;
		private String channelTitle;
	}
}
