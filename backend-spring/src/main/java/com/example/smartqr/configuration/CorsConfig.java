package com.example.smartqr.configuration;

import org.apache.catalina.filters.CorsFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

//@Configuration
public class CorsConfig {
    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

//    @Bean
//    public CorsFilter corsFilter() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//
//        // Allow credentials
//        config.setAllowCredentials(true);
//
//        // Set allowed origins
//        String[] origins = allowedOrigins.split(",");
//        config.setAllowedOriginPatterns(Arrays.asList(origins));
//
//        // Allow all headers
//        config.addAllowedHeader("*");
//
//        // Allow all methods
//        config.addAllowedMethod("*");
//
//        source.registerCorsConfiguration("/**", config);
//        return new CorsFilter();
//    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials
        config.setAllowCredentials(true);

        // Parse and add allowed origins
        if (allowedOrigins.equals("*")) {
            config.addAllowedOriginPattern("*");
        } else {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            origins.forEach(origin -> config.addAllowedOriginPattern(origin.trim()));
        }

        // Allow all headers
        config.addAllowedHeader("*");

        // Allow all methods
        config.addAllowedMethod("*");

        // Expose headers
        config.addExposedHeader("Authorization");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter();
    }
}
