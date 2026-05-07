package com.example.tune_share_hub_backend.controller.auth;

import com.example.tune_share_hub_backend.dto.auth.LoginRequestDto;
import com.example.tune_share_hub_backend.dto.auth.LoginResponseDto;
import com.example.tune_share_hub_backend.dto.user.UserResponseDto;
import com.example.tune_share_hub_backend.global.util.CookieUtil;
import com.example.tune_share_hub_backend.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "로그인", description = "사용자가 이메일과 비밀번호를 입력합니다.")
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(
            @Valid  @RequestBody LoginRequestDto request,
            HttpServletResponse response){
        LoginResponseDto loginResponse = authService.login(request);
        setTokenResponse(response, loginResponse);
        log.info("Login attempt for email: {}", request.getEmail());
        return ResponseEntity.ok(loginResponse.getUserResponseDto());
    }

//    @PostMapping("/reissue")
//    public ResponseEntity<?> reissue(
//            HttpServletRequest request,
//            HttpServletResponse response
//    ){
//        String refresh = cookieUtil.getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
//        LoginRequestDto loginRequestDto = authService.reissue(refresh);
//        setTokenResponse(response, loginRequestDto);
//        log.info("Reissue attempt for email: {}", loginRequestDto.getEmail());
//        return ResponseEntity.ok().build();
//    }

    private void setTokenResponse(HttpServletResponse response, LoginResponseDto loginResponse){
        response.setHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + loginResponse.getAccessToken());
        response.addCookie(authService.createRefreshTokenCookie(loginResponse.getRefreshToken()));
    }
}
