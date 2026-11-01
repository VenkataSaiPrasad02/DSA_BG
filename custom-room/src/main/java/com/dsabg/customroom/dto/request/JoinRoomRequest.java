
package com.dsabg.customroom.dto.request;

public record JoinRoomRequest(
        Long roomCode,
        String password
) {
}
