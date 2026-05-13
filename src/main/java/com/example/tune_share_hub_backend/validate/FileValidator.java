package com.example.tune_share_hub_backend.validate;

import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator {

    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FILE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }

    public static void failFileUpload() {
        throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
    }
}
