package com.dsabg.dsabgauthservice.event;

public record UserRegisteredEvent(
        String userId,
        String username
) {
}
