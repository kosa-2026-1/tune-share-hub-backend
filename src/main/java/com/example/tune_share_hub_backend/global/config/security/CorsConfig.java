package com.example.tune_share_hub_backend.global.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",// 프론트
                        "http://localhost:8080" // Swagger
                )
                .allowedMethods("*")// 허용할 HTTP 메서드
                .allowedHeaders("*")// 요청 헤더 전체 허용
                .exposedHeaders("Authorization")// 프론트에서 Authorization 응답 헤더 읽을 수 있게 함
                .allowCredentials(true);// JWT + 쿠키 인증 시 필요
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/");

    }
}
