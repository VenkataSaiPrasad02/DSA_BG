package com.dsabg.customroom.dto.request;

public record CreateRoomRequest(
        String roomName,
        String password
) {
}
