package com.example.tune_share_hub_backend.global.config.security;

import com.example.tune_share_hub_backend.global.interceptor.AccessTokenCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {

    //JWT 검증 인터셉터
    private final AccessTokenCheckInterceptor atci;

    // 인터셉터를 Spring MVC 요청 처리 흐름에 등록
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(atci)
                .addPathPatterns("/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
