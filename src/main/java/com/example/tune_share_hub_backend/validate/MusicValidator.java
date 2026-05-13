package com.example.tune_share_hub_backend.validate;

import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;

public class MusicValidator {

    public static void validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }
}
