package com.example.tune_share_hub_backend.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import com.example.tune_share_hub_backend.convert.SpotifyTrackConverter;
import com.example.tune_share_hub_backend.dto.music.MusicSearchResponseDto;
import com.example.tune_share_hub_backend.dto.music.SpotifyTrackSearchResponseDto;
import com.example.tune_share_hub_backend.type.SpotifySearchType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpotifyClient {

	private final RestClient.Builder restClientBuilder;

	@Value("${spotify.client-id}")
	private String clientId;

	@Value("${spotify.client-secret}")
	private String clientSecret;

	@Value("${spotify.token-url}")
	private String tokenUrl;

	@Value("${spotify.search-url}")
	private String searchUrl;

	public List<MusicSearchResponseDto> searchTracks(String keyword) {
		String accessToken = getAccessToken();

		SpotifyTrackSearchResponseDto response = restClientBuilder.build()
			.get()
			.uri(searchUrl + "?q={keyword}&type={type}&market={market}&limit={limit}&locale={locale}",
				keyword,
				SpotifySearchType.TRACK.getValue(),
				"KR",
				5,
				"ko-KR")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.header(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
			.retrieve()
			.body(SpotifyTrackSearchResponseDto.class);

		return SpotifyTrackConverter.toMusicSearchResponseDtos(response);
	}

	private String getAccessToken() {
		String authValue = clientId + ":" + clientSecret;
		String encodedAuthValue = Base64.getEncoder()
			.encodeToString(authValue.getBytes(StandardCharsets.UTF_8));

		LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "client_credentials");

		Map<String, Object> response = restClientBuilder.build()
			.post()
			.uri(tokenUrl)
			.header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuthValue)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(body)
			.retrieve()
			.body(Map.class);

		return response.get("access_token").toString();
	}
}
