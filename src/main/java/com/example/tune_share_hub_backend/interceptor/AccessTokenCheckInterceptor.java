package com.example.tune_share_hub_backend.interceptor;

import com.example.tune_share_hub_backend.exception.ApiException;
import com.example.tune_share_hub_backend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AccessTokenCheckInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;

    public AccessTokenCheckInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 정적 리소스 같은 요청은 컨트롤러 메서드가 아니므로 검사하지 않는다.
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // @AccessTokenCheck가 붙은 컨트롤러 메서드만 JWT 검증 대상이다.
        AccessTokenCheck accessTokenCheck = handlerMethod.getMethodAnnotation(AccessTokenCheck.class);
        if (accessTokenCheck == null) {
            return true;
        }

        String accessToken = extractAccessToken(request);
        if (accessToken == null || accessToken.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증 토큰이 필요합니다.");
        }

        Long userId = jwtService.getUserId(accessToken);
        // 컨트롤러에서 request.getAttribute("userId")로 로그인 사용자 ID를 꺼낼 수 있게 저장한다.
        request.setAttribute("userId", userId);
        return true;
    }

    private String extractAccessToken(HttpServletRequest request) {
        // 일반 API 요청은 Authorization: Bearer <token> 헤더로 토큰을 받는다.
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        // 이미지 URL처럼 헤더를 붙이기 어려운 요청을 고려해 쿼리 파라미터도 허용한다.
        return request.getParameter("accessToken");
    }
}
