package com.example.tune_share_hub_backend.dto.music;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "플레이리스트 트랙 추가 요청")
public class PlaylistTrackCreateRequestDto {
	@Schema(description = "Spotify 트랙 ID", example = "5XeFesFbtLpXzIVDNQP22n", requiredMode = Schema.RequiredMode.REQUIRED)
	private String trackId;

	@Schema(description = "곡 제목", example = "I Wanna Be Yours", requiredMode = Schema.RequiredMode.REQUIRED)
	private String title;

	@Schema(description = "아티스트명", example = "Arctic Monkeys", requiredMode = Schema.RequiredMode.REQUIRED)
	private String artistName;

	@Schema(description = "앨범명", example = "AM")
	private String albumName;

	@Schema(description = "앨범 이미지 URL", example = "https://i.scdn.co/image/ab67616d0000b2734ae1c4c5c45aabe565499163")
	private String albumImageUrl;

	@Schema(description = "Spotify 곡 URL", example = "https://open.spotify.com/track/5XeFesFbtLpXzIVDNQP22n")
	private String spotifyUrl;

	@Schema(description = "곡 재생 시간(ms)", example = "183956")
	private Long durationMs;

	@Setter
	@Schema(description = "YouTube 영상 URL", example = "https://www.youtube.com/watch?v=nyuo9-OjNNg")
	private String youtubeUrl;

}
