package com.example.tune_share_hub_backend.controller.auth;

import com.example.tune_share_hub_backend.dto.auth.LoginRequestDto;
import com.example.tune_share_hub_backend.dto.auth.LoginResponseDto;
import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.user.UserResponseDto;
import com.example.tune_share_hub_backend.global.interceptor.AccessTokenCheck;
import com.example.tune_share_hub_backend.global.interceptor.LoginUserId;
import com.example.tune_share_hub_backend.global.util.CookieUtil;
import com.example.tune_share_hub_backend.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Operation(summary = "로그인", description = "사용자가 이메일과 비밀번호를 입력하여 로그인합니다. 성공 시 Access Token과 Refresh Token이 발급됩니다.")
    @PostMapping("/login")
    public ApiResponseDto<UserResponseDto> login(
            @Valid  @RequestBody LoginRequestDto request,
            HttpServletResponse response){
        LoginResponseDto loginResponse = authService.login(request);
        setTokenResponse(response, loginResponse);
        log.info("Login attempt for email: {}", request.getEmail());
        return ApiResponseDto.success(loginResponse.getUserResponseDto(), "로그인 성공");
    }

    @Operation(summary = "토큰 재발급", description = "Access Token이 만료되었을 때, Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @PostMapping("/reissue")
    public ApiResponseDto<Void> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        String refresh = cookieUtil.getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
        LoginResponseDto loginResponseDto = authService.reissue(refresh);
        setTokenResponse(response, loginResponseDto);
        log.info("Reissue attempt for email: {}", loginResponseDto.getUserResponseDto().getEmail());
        return ApiResponseDto.success(null, "토큰 재발급 성공");
    }

    @Operation(summary = "로그아웃", description = "사용자가 로그아웃할 때, Access Token과 Refresh Token을 모두 무효화합니다.")
    @PostMapping("/logout")
    @AccessTokenCheck
    public ApiResponseDto<Void> logout(
            @LoginUserId Long userId,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        String refresh = cookieUtil.getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
        try{
            authService.logout(refresh, userId);
        }finally {
            cookieUtil.deleteCookie(response, REFRESH_TOKEN_COOKIE_NAME);
        }
        return ApiResponseDto.success(null, "로그아웃 성공");
    }

    private void setTokenResponse(HttpServletResponse response, LoginResponseDto loginResponse){
        response.setHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + loginResponse.getAccessToken());
        response.addCookie(authService.createRefreshTokenCookie(loginResponse.getRefreshToken()));
    }
}
