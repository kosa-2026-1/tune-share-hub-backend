package com.example.tune_share_hub_backend.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tune Share Hub API")
                        .description("Tune Share Hub 프로젝트 API 문서입니다.")
                        .version("v1.0.0"));
    }
}
