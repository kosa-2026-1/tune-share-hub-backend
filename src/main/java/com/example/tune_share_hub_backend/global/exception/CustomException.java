package com.example.tune_share_hub_backend.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private Object data;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, Object data) {
        super(errorCode.getMessage() + data.toString());
        this.errorCode = errorCode;
        this.data = data;
    }
}
