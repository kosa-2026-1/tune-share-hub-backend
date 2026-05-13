package com.example.tune_share_hub_backend.validate;

import com.example.tune_share_hub_backend.dto.auth.LoginRequestDto;
import com.example.tune_share_hub_backend.entity.user.User;
import com.example.tune_share_hub_backend.global.config.security.JwtProvider;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthValidator {

    public static void validateLoginUser(User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    public static void validatePassword(LoginRequestDto loginRequestDto, User user, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
    }

    public static void validateLogoutRequest(String refreshToken, Long userId) {
        if (refreshToken == null || refreshToken.isBlank() || userId == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    public static void validateRefreshTokenCategory(boolean refreshToken) {
        if (!refreshToken) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    public static void validateRefreshToken(String refreshToken, JwtProvider jwtProvider) {
        jwtProvider.validateToken(refreshToken);
        validateRefreshTokenCategory(jwtProvider.isRefreshToken(refreshToken));
    }

    public static void validateRefreshTokenExists(boolean exists) {
        if (!exists) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }
    }

    public static void validateRotateTokenRequest(Long userId, String oldToken, String newToken) {
        if (userId == null || oldToken == null || newToken == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    public static void validateRefreshTokenDeleted(int deletedCount) {
        if (deletedCount == 0) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }
    }

    public static void validateRefreshTokenRevoked(int revokedCount) {
        if (revokedCount == 0) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
