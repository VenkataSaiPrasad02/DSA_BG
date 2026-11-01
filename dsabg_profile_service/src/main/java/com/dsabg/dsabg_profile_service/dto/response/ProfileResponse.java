package com.dsabg.dsabg_profile_service.dto.response;


import java.time.LocalDateTime;

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

