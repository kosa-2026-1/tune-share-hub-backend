package com.example.tune_share_hub_backend.service.auth;

import com.example.tune_share_hub_backend.convert.AuthConvert;
import com.example.tune_share_hub_backend.convert.UserConvert;
import com.example.tune_share_hub_backend.dao.user.UserDao;
import com.example.tune_share_hub_backend.dto.auth.LoginRequestDto;
import com.example.tune_share_hub_backend.dto.auth.LoginResponseDto;
import com.example.tune_share_hub_backend.dto.user.UserResponseDto;
import com.example.tune_share_hub_backend.entity.user.User;
import com.example.tune_share_hub_backend.global.config.security.JwtProvider;
import com.example.tune_share_hub_backend.global.util.CookieUtil;
import com.example.tune_share_hub_backend.validate.AuthValidator;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        User user = findLoginUser(loginRequestDto);

        UserResponseDto userResponseDto = UserConvert.toResponseDto(user);
        String accessToken = jwtProvider.createAccessToken(userResponseDto);
        String refreshToken = jwtProvider.createRefreshToken(userResponseDto);
        LocalDateTime expiresAt = jwtProvider.getExpirationDateTime(refreshToken);

        refreshTokenService.saveRefreshToken(
                user.getUserId(),
                refreshToken,
                expiresAt
        );

        return AuthConvert.toLoginResponseDto(accessToken, refreshToken, userResponseDto);
    }

    @Transactional
    public LoginResponseDto reissue(String refreshToken){
        AuthValidator.validateRefreshToken(refreshToken, jwtProvider);

        Long userId = jwtProvider.getUserId(refreshToken);
        refreshTokenService.checkRefreshTokenExists(refreshToken, userId);

        User user = userDao.getUserById(userId);
        UserResponseDto userResponseDto = UserConvert.toResponseDto(user);

        String newAccessToken = jwtProvider.createAccessToken(userResponseDto);
        String newRefreshToken = jwtProvider.createRefreshToken(userResponseDto);
        LocalDateTime expiresAt = jwtProvider.getExpirationDateTime(newRefreshToken);

        refreshTokenService.rotateToken(
                userId,
                refreshToken,
                newRefreshToken,
                expiresAt
        );

        return AuthConvert.toLoginResponseDto(newAccessToken, newRefreshToken, userResponseDto);
    }

    @Transactional
    public void logout(String refreshToken, Long userId) {
        AuthValidator.validateLogoutRequest(refreshToken, userId);
        refreshTokenService.revokeToken(userId, refreshToken);
    }

    private User findLoginUser(LoginRequestDto loginRequestDto) {
        User user = userDao.getUserByEmail(loginRequestDto.getEmail());
        AuthValidator.validateLoginUser(user);
        AuthValidator.validatePassword(loginRequestDto, user, passwordEncoder);
        return user;
    }

    public Cookie createRefreshTokenCookie(String refreshToken) {
        return cookieUtil.createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, refreshExpiration);
    }
}
