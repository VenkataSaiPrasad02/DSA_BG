package com.dsabg.dsabgfriendsservice.dto.response;

public record FriendListProfileResponse(
        String userId,
        String displayName,
        String avatarUrl

) {
}