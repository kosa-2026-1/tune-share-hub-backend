package com.example.tune_share_hub_backend.service.auth;

import com.example.tune_share_hub_backend.convert.AuthConvert;
import com.example.tune_share_hub_backend.convert.UserConvert;
import com.example.tune_share_hub_backend.dao.user.UserDao;
import com.example.tune_share_hub_backend.dto.auth.LoginRequestDto;
import com.example.tune_share_hub_backend.dto.auth.LoginResponseDto;
import com.example.tune_share_hub_backend.dto.user.UserResponseDto;
import com.example.tune_share_hub_backend.entity.user.User;
import com.example.tune_share_hub_backend.global.config.security.JwtProvider;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import com.example.tune_share_hub_backend.global.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
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
        // 사용자 조회 및 검증
        User user = findAndValidateUser(loginRequestDto);
        
        // 토큰 생성
        UserResponseDto userResponseDto = UserConvert.toResponseDto(user);
        String accessToken = jwtProvider.createAccessToken(userResponseDto);
        String refreshToken = jwtProvider.createRefreshToken(userResponseDto);

        LocalDateTime expiresAt = jwtProvider.getExpirationDateTime(refreshToken);

        log.info("User {} logged in successfully", user.getEmail());

        refreshTokenService.saveRefreshToken(
                user.getUserId(),
                refreshToken,
                expiresAt
        );

        return AuthConvert.toLoginResponseDto(accessToken, refreshToken, userResponseDto);
    }

    @Transactional
    public LoginResponseDto reissue(String refreshToken){
        // 토큰 유효성 체크
        validateRefreshToken(refreshToken);

        Long userId = jwtProvider.getUserId(refreshToken);

        refreshTokenService.validateRefreshTokenExists(refreshToken, userId);

        // 사용자 조회
        User user = userDao.getUserById(userId);
        UserResponseDto userResponseDto = UserConvert.toResponseDto(user);

        // 토큰 재발급
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

        if (refreshToken == null || refreshToken.isBlank() || userId == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        refreshTokenService.revokeToken(userId, refreshToken);

        log.info("User {} logged out", userId);
    }

    //토큰 유효성 체크
    private void validateRefreshToken(String refreshToken) {
        jwtProvider.validateToken(refreshToken);

        if (!jwtProvider.isRefreshToken(refreshToken)) {
            log.warn("Not a refresh token category");
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }


    // 사용자 존재 여부 및 비밀번호 검증
    private User findAndValidateUser(LoginRequestDto loginRequestDto) {
        User user = userDao.getUserByEmail(loginRequestDto.getEmail());

        if (user == null) {
            log.warn("Login attempt for non-existent user: {}", loginRequestDto.getEmail());
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for user: {} - invalid password", loginRequestDto.getEmail());
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        return user;
    }

    public Cookie createRefreshTokenCookie(String refreshToken) {
        return cookieUtil.createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, refreshExpiration);
    }
}
