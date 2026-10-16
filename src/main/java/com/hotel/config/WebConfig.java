package com.hotel.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String[] allowedOrigins;
    public WebConfig(@Value("${hotel.cors.allowed-origins}") String origins) {
        allowedOrigins = Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        if (Arrays.asList(allowedOrigins).contains("*")) throw new IllegalArgumentException("Explicit CORS origins are required");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "OPTIONS").allowedHeaders("Content-Type", "Accept")
                .exposedHeaders("Location").maxAge(3600);
    }
}
