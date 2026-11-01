package com.dsabg.customroom.dto.response;

import java.time.LocalDateTime;

public record RoomMemberResponse(
        String userId,
        String  displayName,
        String team,
        Integer slotNumber,
        LocalDateTime joinedAt
) {
}
