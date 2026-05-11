package com.example.tune_share_hub_backend.entity;

import lombok.Getter;

@Getter
public enum SpotifySearchType {

	ALBUM("album"),
	ARTIST("artist"),
	PLAYLIST("playlist"),
	TRACK("track"),
	SHOW("show"),
	EPISODE("episode"),
	AUDIOBOOK("audiobook");

	private final String value;

	SpotifySearchType(String value) {
		this.value = value;
	}

}

