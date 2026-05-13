package com.example.tune_share_hub_backend.validate;

import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;

public class LikeValidator {

    public static void validateLikablePlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new CustomException(ErrorCode.PLAYLIST_NOT_FOUND);
        }

        if ("N".equals(playlist.getPublicYn())) {
            throw new CustomException(ErrorCode.PRIVATE_PLAYLIST_CANNOT_BE_LIKED);
        }
    }
}
