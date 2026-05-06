package com.example.tune_share_hub_backend.dto.user;

import com.example.tune_share_hub_backend.entity.user.User;
import com.example.tune_share_hub_backend.entity.user.UserRoleType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserResponseDto {
    private Long userId;
    private String email;
    private String nickname;
    private UserRoleType role;

    public static UserResponseDto from(User user){
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }
}
