package com.example.tune_share_hub_backend.dto.auth;

import com.example.tune_share_hub_backend.dto.user.UserResponseDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private UserResponseDto userResponseDto;
}
