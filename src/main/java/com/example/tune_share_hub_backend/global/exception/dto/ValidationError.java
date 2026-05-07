package com.example.tune_share_hub_backend.global.exception.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ValidationError {
    private final String field;
    private final String message;
}
