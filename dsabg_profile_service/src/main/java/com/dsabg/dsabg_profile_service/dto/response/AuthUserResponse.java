package com.dsabg.dsabg_profile_service.dto.response;

public record AuthUserResponse(
        String userId,
        String username,
        String email
) {
}
