package com.example.tune_share_hub_backend.convert;

import com.example.tune_share_hub_backend.dto.auth.LoginResponseDto;
import com.example.tune_share_hub_backend.dto.user.UserResponseDto;

public class AuthConvert {

    public static LoginResponseDto toLoginResponseDto(String accessToken, String refreshToken,
                                                      UserResponseDto userResponseDto) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userResponseDto(userResponseDto)
                .build();
    }
}
