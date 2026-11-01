package com.dsabg.customroom.dto.response;

public record RoomEvent(
        Long roomCode,
        String eventType,
        String userId,
        String message
) {
}
