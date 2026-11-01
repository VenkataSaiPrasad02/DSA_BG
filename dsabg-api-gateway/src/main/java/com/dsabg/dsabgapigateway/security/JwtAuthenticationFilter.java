package com.dsabg.dsabgapigateway.security;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class JwtAuthenticationFilter implements WebFilter {

    private static final String GATEWAY_TRUST_HEADER =
            "X-DSABG-Gateway-Trusted";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/auth/") || path.equals("/auth")
                || path.equals("/ws") || path.startsWith("/ws/")) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authorization.substring(7).trim();

        if (token.isBlank()) {
            return unauthorized(exchange);
        }

        try {
            if (!jwtService.isTokenValid(token)) {
                return unauthorized(exchange);
            }

            String userId = jwtService.extractUserId(token);
            String username = jwtService.extractUserName(token);

            if (userId == null || userId.isBlank()
                    || username == null || username.isBlank()) {
                return unauthorized(exchange);
            }

            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Name");
                        headers.remove("X-DSABG-Gateway-Trusted");

                        headers.set("X-User-Id", userId);
                        headers.set("X-User-Name", username);
                        headers.set("X-DSABG-Gateway-Trusted", "gateway");
                    })
                    .build();

            return chain.filter(
                    exchange.mutate()
                            .request(request)
                            .build()
            );

        } catch (JwtException | IllegalArgumentException ex) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}