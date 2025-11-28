package org.autostock.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:#{null}}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String allowedOrigin = (frontendUrl != null) ? frontendUrl : "http://localhost:3000";
        System.out.println("✅ CORS Config actif pour: " + allowedOrigin);
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:4200", "http://localhost:3000", "ws://localhost:8080", allowedOrigin)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
