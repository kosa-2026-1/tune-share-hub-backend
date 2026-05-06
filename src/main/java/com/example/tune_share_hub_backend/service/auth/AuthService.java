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
@Transactional(readOnly = true)
public class AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private final CookieUtil cookieUtil;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto){
        User user = userDao.getUserByEmail(loginRequestDto.getEmail());
        //1. 아이디 확인
        if(user==null){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        //2. 비밀번호 확인
        if(!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())){
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        log.info("User {} logged in successfully", user.getEmail());

        UserResponseDto userResponseDto = UserResponseDto.from(user);
        String accessToken = jwtProvider.createJwt(TOKEN_TYPE_ACCESS, userResponseDto);
        String refreshToken = jwtProvider.createJwt(TOKEN_TYPE_REFRESH, userResponseDto);

        //3. 토큰 생성
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userResponseDto(userResponseDto)
                .build();
    }

    public Cookie createRefreshTokenCookie(String refresh) {
        return cookieUtil.createCookie(REFRESH_TOKEN_COOKIE_NAME, refresh, refreshExpiration);
    }
}
