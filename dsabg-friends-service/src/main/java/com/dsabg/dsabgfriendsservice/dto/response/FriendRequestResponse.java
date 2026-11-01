package com.dsabg.dsabgfriendsservice.dto.response;

import com.dsabg.dsabgfriendsservice.entity.FriendRequestStatus;

public record FriendRequestResponse(
        String message,
        FriendRequestStatus  status
) {
}
