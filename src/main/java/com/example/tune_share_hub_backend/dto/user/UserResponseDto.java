package com.example.tune_share_hub_backend.dto.user;

import com.example.tune_share_hub_backend.entity.user.UserRoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(description = "사용자 응답")
public class UserResponseDto {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 이메일", example = "test@example.com")
    private String email;

    @Schema(description = "사용자 닉네임", example = "테스트유저")
    private String nickname;

    @Schema(description = "사용자 권한", example = "USER")
    private UserRoleType role;
}
