
package com.dsabg.dsabgapigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }
    public String extractUserName(String token) {
        return extractClaims(token).get("username").toString();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);

            return claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());

        } catch (ExpiredJwtException e) {
            System.out.println("JWT ERROR: Token expired");
        } catch (SignatureException e) {
            System.out.println("JWT ERROR: Signature mismatch");
        } catch (JwtException e) {
            System.out.println("JWT ERROR: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT ERROR: Invalid token or key");
        }

        return false;
    }
}
