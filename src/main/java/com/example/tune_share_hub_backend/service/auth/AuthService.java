package com.example.tune_share_hub_backend.service.auth;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        // 사용자 조회 및 검증
        User user = findAndValidateUser(loginRequestDto);
        
        // 토큰 생성
        UserResponseDto userResponseDto = UserResponseDto.from(user);
        String accessToken = jwtProvider.createAccessToken(userResponseDto);
        String refreshToken = jwtProvider.createRefreshToken(userResponseDto);

        log.info("User {} logged in successfully", user.getEmail());

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userResponseDto(userResponseDto)
                .build();
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
