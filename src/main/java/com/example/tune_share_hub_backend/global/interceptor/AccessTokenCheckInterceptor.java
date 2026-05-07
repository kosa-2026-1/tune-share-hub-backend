package com.example.tune_share_hub_backend.global.interceptor;

import com.example.tune_share_hub_backend.global.config.security.JwtProvider;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccessTokenCheckInterceptor implements HandlerInterceptor {
    private final JwtProvider jwtProvider;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle");

        // HandlerMethod 체크
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 엔드포인트에 @AccessTokenCheck가 붙어있는지 확인
        AccessTokenCheck accessTokenCheck = handlerMethod.getMethodAnnotation(AccessTokenCheck.class);
        if (accessTokenCheck == null) {
            return true;
        }

        // @AccessTokenCheck가 붙어있는 경우, AccessToken 추출 및 검증
        String accessToken = extractAndValidateToken(request);

        // 유효한 토큰일 경우, 사용자 정보를 request에 설정
        Long userId = jwtProvider.getUserId(accessToken);
        String email = jwtProvider.getEmail(accessToken);
        String nickname = jwtProvider.getNickname(accessToken);
        String role = jwtProvider.getRole(accessToken);

        request.setAttribute("userId", userId);
        request.setAttribute("email", email);
        request.setAttribute("nickname", nickname);
        request.setAttribute("role", role);

        return true;
    }

    // Authorization 헤더에서 토큰 추출 및 검증
    private String extractAndValidateToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or invalid Authorization header");
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        String accessToken = authorization.substring(BEARER_PREFIX.length());

        // 토큰 검증 (validateToken은 CustomException을 던짐)
        jwtProvider.validateToken(accessToken);

        return accessToken;
    }
}
