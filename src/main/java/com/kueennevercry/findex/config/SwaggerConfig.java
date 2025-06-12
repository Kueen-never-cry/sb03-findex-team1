package com.kueennevercry.findex.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "Findex API",
        version = "v1.0.0",
        description = "가볍고 빠른 외부 API 연동 금융 분석 도구 API 문서"
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Local development server"
        ),
        @Server(
            url = "배포url",
            description = "Production server"
        )
    }
)
@Configuration
public class SwaggerConfig {

}