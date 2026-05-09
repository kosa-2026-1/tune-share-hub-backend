package com.example.tune_share_hub_backend.convert;

import com.example.tune_share_hub_backend.dto.music.MusicSearchResponseDto;
import com.example.tune_share_hub_backend.dto.music.SpotifyTrackSearchResponseDto;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class SpotifyTrackConverter {

    public static List<MusicSearchResponseDto> toMusicSearchResponseDtos(
            SpotifyTrackSearchResponseDto response
    ) {
        List<MusicSearchResponseDto> result = new ArrayList<>();

        if (response == null || response.getTracks() == null) {
            return result;
        }

        List<SpotifyTrackSearchResponseDto.TrackItem> items =
                response.getTracks().getItems();

        if (items == null || items.isEmpty()) {
            return result;
        }

        for (SpotifyTrackSearchResponseDto.TrackItem item : items) {
            result.add(toMusicSearchResponseDto(item));
        }

        return result;
    }

    public static MusicSearchResponseDto toMusicSearchResponseDto(
            SpotifyTrackSearchResponseDto.TrackItem item
    ) {
        String albumName = "";
        String albumImageUrl = "";

        if (item.getAlbum() != null) {
            albumName = item.getAlbum().getName();
            albumImageUrl = item.getAlbum().getFirstImageUrl();
        }

        return new MusicSearchResponseDto(
                item.getId(),
                item.getName(),
                item.getFirstArtistName(),
                albumName,
                albumImageUrl,
                item.getSpotifyUrl(),
                item.getPreviewUrl(),
                item.getDurationMs()
        );
    }


}
