package com.dsabg.dsabgfriendsservice.dto.response;

public record ProfileResponse(
        String userId,
        String displayName,
        String avatarUrl,
        String bio,
        int level,
        int battlesPlayed,
        int wins,
        int losses,
        int problemsSolved
) {
}
