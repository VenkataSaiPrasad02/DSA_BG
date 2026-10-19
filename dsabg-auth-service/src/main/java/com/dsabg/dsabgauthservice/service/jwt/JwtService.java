package com.dsabg.dsabgauthservice.service.jwt;

public interface JwtService {
    String generateToken(String userId, String email);
}
