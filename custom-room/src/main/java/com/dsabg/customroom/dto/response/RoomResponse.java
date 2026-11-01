package com.dsabg.customroom.dto.response;

import com.dsabg.customroom.entity.enums.RoomStatus;

import java.time.LocalDateTime;
import java.util.List;

public record RoomResponse(
        Long roomCode,
        String roomName,
        RoomStatus roomStatus,
        String hostUserId,
        String hostDisplayName,
        LocalDateTime createdAt,
        List<RoomMemberResponse> roomMembers
) {
}
