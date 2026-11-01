package com.dsabg.dsabgfriendsservice.dto.response;

import com.dsabg.dsabgfriendsservice.entity.FriendShipStatus;

public record PlayerSearchResponse(
        FriendListProfileResponse profile,
        FriendShipStatus relationship
) {
}
