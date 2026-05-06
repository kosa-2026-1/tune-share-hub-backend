package com.example.tune_share_hub_backend.client;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.tune_share_hub_backend.dto.music.MusicSearchResponseDto;
import com.example.tune_share_hub_backend.dto.music.YoutubeSearchResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class YoutubeClient {

	private final RestClient restClient = RestClient.create();

	@Value("${youtube.api-key}")
	private String apiKey;

	@Value("${youtube.search-url}")
	private String searchUrl;

	public YoutubeSearchResponseDto searchMusicVideo(String keyword) {
		URI uri = UriComponentsBuilder
			.fromUriString(searchUrl)
			.queryParam("part", "snippet")
			.queryParam("q", keyword)
			.queryParam("type", "video")
			.queryParam("maxResults", 5)
			.queryParam("regionCode", "KR")
			.queryParam("key", apiKey)
			.build()
			.encode()
			.toUri();

		log.info("youtube keyword = {}", keyword);
		log.info("youtube request url = {}", maskApiKey(uri.toString()));

		YoutubeSearchResponseDto response = restClient.get()
			.uri(uri)
			.retrieve()
			.body(YoutubeSearchResponseDto.class);

		logYoutubeItems(response);

		return response;
	}

	public void addVideoUrl(List<MusicSearchResponseDto> musicSearchResponseDtos) {
		for (MusicSearchResponseDto dto : musicSearchResponseDtos) {
			String keyword = dto.getTitle() + " " + dto.getArtistName() + " 뮤직비디오";

			YoutubeSearchResponseDto youtubeResponse = searchMusicVideo(keyword);
			String videoId = extractFirstVideoId(youtubeResponse);

			if (videoId == null) {
				dto.setYoutubeUrl(null);
				continue;
			}

			dto.setYoutubeUrl("https://www.youtube.com/embed/" + videoId);
		}
	}

	private String extractFirstVideoId(YoutubeSearchResponseDto response) {
		if (response == null) {
			log.info("youtube response is null");
			return null;
		}

		if (response.getItems() == null || response.getItems().isEmpty()) {
			log.info("youtube items is empty");
			return null;
		}

		YoutubeSearchResponseDto.YoutubeItemDto item = response.getItems().get(0);

		if (item.getId() == null || item.getId().getVideoId() == null) {
			log.info("youtube videoId is null");
			return null;
		}

		log.info("selected youtube videoId = {}", item.getId().getVideoId());

		return item.getId().getVideoId();
	}

	private void logYoutubeItems(YoutubeSearchResponseDto response) {
		if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
			log.info("youtube items is empty");
			return;
		}

		for (int i = 0; i < response.getItems().size(); i++) {
			YoutubeSearchResponseDto.YoutubeItemDto item = response.getItems().get(i);

			String videoId = item.getId() == null ? null : item.getId().getVideoId();
			String title = item.getSnippet() == null ? null : item.getSnippet().getTitle();
			String channelTitle = item.getSnippet() == null ? null : item.getSnippet().getChannelTitle();

			log.info("youtube item[{}] videoId = {}", i, videoId);
			log.info("youtube item[{}] title = {}", i, title);
			log.info("youtube item[{}] channelTitle = {}", i, channelTitle);
		}
	}

	private String maskApiKey(String url) {
		return url.replaceAll("key=[^&]+", "key=****");
	}
}