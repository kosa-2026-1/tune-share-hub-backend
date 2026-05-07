package com.example.tune_share_hub_backend.global.config.security;

import com.example.tune_share_hub_backend.global.interceptor.AccessTokenCheckInterceptor;
import com.example.tune_share_hub_backend.global.interceptor.LoginUserIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {

    //JWT 검증 인터셉터
    private final AccessTokenCheckInterceptor atci;
    private final LoginUserIdArgumentResolver luar;

    // 인터셉터를 Spring MVC 요청 처리 흐름에 등록
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(atci)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/reissue",

                        // swagger
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",

                        // 에러
                        "/error"
                );
    }

    // 로그인한 사용자 ID를 컨트롤러 메서드 인자로 주입하는 ArgumentResolver 등록
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(luar);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
