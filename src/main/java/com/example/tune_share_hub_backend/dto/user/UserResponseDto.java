package com.example.tune_share_hub_backend.dto.user;

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
}
