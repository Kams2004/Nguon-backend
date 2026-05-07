package com.example.ngoun.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration open = new CorsConfiguration();
        open.setAllowedOriginPatterns(List.of("*"));
        open.setAllowedMethods(List.of("GET"));
        open.setAllowedHeaders(List.of("*"));

        CorsConfiguration restricted = new CorsConfiguration();
        restricted.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080",
                "http://10.237.18.152:8080",
                "https://www.nguonevents.com",
                "http://www.nguonevents.com",
                "http://167.86.120.214"
        ));
        restricted.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        restricted.setAllowedHeaders(List.of("*"));
        restricted.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/actualities/preview/**", open);
        source.registerCorsConfiguration("/**", restricted);
        return source;
    }
}
