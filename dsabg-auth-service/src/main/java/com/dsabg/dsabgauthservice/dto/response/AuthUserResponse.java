package com.dsabg.dsabgauthservice.dto.response;

public record AuthUserResponse(
        String userId,
        String username,
        String email
) {
}
