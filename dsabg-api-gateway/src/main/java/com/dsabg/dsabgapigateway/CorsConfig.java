package com.dsabg.dsabgapigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Gateway CORS configuration.
 *
 * Rules:
 *  - Exact frontend origin only (no wildcard) — required for credentials.
 *  - allowCredentials = true — required for cookie + XSRF header delivery.
 *  - X-XSRF-TOKEN is allowed as a request header (covered by allowedHeaders=*).
 *  - No setExposedHeaders needed: XSRF-TOKEN travels as a cookie, not a
 *    response header, so the browser can read it without explicit exposure.
 */
@Configuration
public class CorsConfig {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Exact origin — wildcard ("*") is forbidden when allowCredentials = true.
        config.setAllowedOrigins(List.of(frontendUrl));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));

        // Required so the browser sends cookies and the X-XSRF-TOKEN header.
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}