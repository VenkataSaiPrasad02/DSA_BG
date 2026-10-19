package com.dsabg.dsabgauthservice.dto.response;

public record AuthResponse(
        String message,
        String token,
        String tokenType
) {}