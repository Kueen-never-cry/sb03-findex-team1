package com.kueennevercry.findex.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "openapi")
public class OpenApiProperties {

  private String baseUrl;
  private String apiKey;
}

