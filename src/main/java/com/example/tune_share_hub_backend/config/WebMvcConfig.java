package com.example.tune_share_hub_backend.config;

import com.example.tune_share_hub_backend.interceptor.AccessTokenCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final AccessTokenCheckInterceptor accessTokenCheckInterceptor;

    public WebMvcConfig(AccessTokenCheckInterceptor accessTokenCheckInterceptor) {
        this.accessTokenCheckInterceptor = accessTokenCheckInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // /api/** 요청 중 @AccessTokenCheck가 붙은 컨트롤러 메서드만 실제로 JWT 검사를 받는다.
        registry.addInterceptor(accessTokenCheckInterceptor)
                .addPathPatterns("/api/**");
    }
}
