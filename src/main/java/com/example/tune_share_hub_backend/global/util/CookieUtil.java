package com.example.tune_share_hub_backend.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    // 쿠키에서 특정 이름의 값 추출
    public String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 쿠키 생성
    public Cookie createCookie(String name, String value, Long maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);// JS 접근 방지 (XSS 방어)
        cookie.setPath("/");
        cookie.setMaxAge((int) (maxAge / 1000));
        // cookie.setSecure(true);// HTTPS 환경 적용 시 활성화
        return cookie;
    }

    // 쿠키 삭제 (로그아웃 시 사용)
    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
