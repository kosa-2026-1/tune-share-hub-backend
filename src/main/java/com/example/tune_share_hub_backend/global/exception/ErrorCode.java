package com.example.tune_share_hub_backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    //Auth & User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),

    //Token
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),


    //Playlist
    INVALID_PLAYLIST_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 플레이리스트 ID입니다."),
    PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "플레이리스트를 찾을 수 없습니다."),
    PLAYLIST_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "플레이리스트를 수정할 수 없습니다."),
    PLAYLIST_VISIBILITY_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "플레이리스트 공개 여부를 변경할 수 없습니다."),
    INVALID_PUBLIC_YN(HttpStatus.BAD_REQUEST, "공개 여부는 Y 또는 N만 가능합니다."),
    INVALID_PLAYLIST_TITLE(HttpStatus.BAD_REQUEST, "플레이리스트 제목은 필수입니다."),
    PLAYLIST_TRACK_NOT_FOUND(HttpStatus.NOT_FOUND, "플레이리스트에 노래를 찾을 수 없습니다." ),

    //Like
    LIKE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 좋아요가 존재합니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;


}
