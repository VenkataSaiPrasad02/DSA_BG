package com.dsabg.dsabgauthservice.service.jwt;

import com.dsabg.dsabgauthservice.entity.Enum.Roles;

public interface JwtService {
    String generateToken(String userId, String username, String email, Roles role);

    String extractUserId(String token);
    boolean isTokenValid(String token);
}
