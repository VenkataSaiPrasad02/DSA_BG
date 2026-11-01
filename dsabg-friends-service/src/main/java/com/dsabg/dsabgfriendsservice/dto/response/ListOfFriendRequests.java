package com.dsabg.dsabgfriendsservice.dto.response;

import com.dsabg.dsabgfriendsservice.entity.FriendRequestStatus;


public record ListOfFriendRequests(
        Long id,
        String senderId,
        FriendRequestStatus status,
        FriendListProfileResponse sender
) {


}
