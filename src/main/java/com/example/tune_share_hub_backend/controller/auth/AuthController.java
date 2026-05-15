package com.example.tune_share_hub_backend.controller.auth;

import com.example.tune_share_hub_backend.dto.auth.LoginRequestDto;
import com.example.tune_share_hub_backend.dto.auth.LoginResponseDto;
import com.example.tune_share_hub_backend.dto.common.ApiResponseDto;
import com.example.tune_share_hub_backend.dto.user.UserResponseDto;
import com.example.tune_share_hub_backend.global.exception.dto.ApiError;
import com.example.tune_share_hub_backend.global.interceptor.AccessTokenCheck;
import com.example.tune_share_hub_backend.global.interceptor.LoginUserId;
import com.example.tune_share_hub_backend.global.util.CookieUtil;
import com.example.tune_share_hub_backend.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "인증 API")
public class AuthController {
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다. 성공 시 Access Token은 응답 헤더에, Refresh Token은 쿠키에 담깁니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/login")
    public ApiResponseDto<UserResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            @Parameter(hidden = true)
            HttpServletResponse response){
        LoginResponseDto loginResponse = authService.login(request);
        setTokenResponse(response, loginResponse);
        log.info("Login attempt for email: {}", request.getEmail());
        return ApiResponseDto.success(loginResponse.getUserResponseDto(), "로그인 성공");
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token 쿠키를 사용해 새로운 Access Token과 Refresh Token을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "Refresh Token이 없거나 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/reissue")
    public ApiResponseDto<Void> reissue(
            @Parameter(hidden = true)
            HttpServletRequest request,
            @Parameter(hidden = true)
            HttpServletResponse response
    ){
        String refresh = cookieUtil.getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
        LoginResponseDto loginResponseDto = authService.reissue(refresh);
        setTokenResponse(response, loginResponseDto);
        log.info("Reissue attempt for email: {}", loginResponseDto.getUserResponseDto().getEmail());
        return ApiResponseDto.success(null, "토큰 재발급 성공");
    }

    @Operation(summary = "로그아웃", description = "로그인한 사용자의 Refresh Token을 무효화하고 쿠키를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "Access Token이 유효하지 않습니다.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/logout")
    @AccessTokenCheck
    public ApiResponseDto<Void> logout(
            @Parameter(hidden = true)
            @LoginUserId Long userId,
            @Parameter(hidden = true)
            HttpServletRequest request,
            @Parameter(hidden = true)
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
