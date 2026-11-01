package com.dsabg.dsabgfriendsservice.service;

import com.dsabg.dsabgfriendsservice.dto.response.FriendRequestResponse;
import com.dsabg.dsabgfriendsservice.dto.response.FriendListProfileResponse;
import com.dsabg.dsabgfriendsservice.dto.response.RelationshipResponse;

import java.util.List;

public interface FriendshipService {

    FriendRequestResponse acceptFriendRequest(Long requestId, String receiverId);


    FriendRequestResponse rejectFriendRequest(Long requestId, String receiverId);

    List<FriendListProfileResponse> getMyFriends(String userId);

    void removeFriend(String userId, String friendId);

    RelationshipResponse getRelationship(
            String currentUserId,
            String targetUserId
    );
}
