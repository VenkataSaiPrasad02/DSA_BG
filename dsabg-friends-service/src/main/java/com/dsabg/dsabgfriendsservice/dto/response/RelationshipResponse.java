package com.dsabg.dsabgfriendsservice.dto.response;

import com.dsabg.dsabgfriendsservice.entity.FriendShipStatus;

public record RelationshipResponse(
        String userId,
        FriendShipStatus status
) {}