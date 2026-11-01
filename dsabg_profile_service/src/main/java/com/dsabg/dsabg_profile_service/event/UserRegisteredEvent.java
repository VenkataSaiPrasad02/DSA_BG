
package com.dsabg.dsabg_profile_service.event;

public record UserRegisteredEvent(
        String userId,
        String username
) {
}