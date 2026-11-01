
package com.dsabg.customroom.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;



@Component
public class JwtFilter {

    private final SecretKey secretKey;

    public JwtFilter(
            @Value("${JWT_SECRET}") String secret) {

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String verifyToken(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Date expiration = claims.getExpiration();

        if (expiration == null || !expiration.after(new Date())) {
            throw new JwtException("Missing or expired token");
        }

        String userId = claims.getSubject();

        if (userId == null || userId.isBlank()) {
            throw new JwtException("Missing user ID");
        }

        return userId;
    }
}
