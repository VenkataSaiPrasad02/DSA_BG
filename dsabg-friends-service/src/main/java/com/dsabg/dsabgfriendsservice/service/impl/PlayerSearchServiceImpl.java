package com.dsabg.dsabgfriendsservice.service.impl;

import com.dsabg.dsabgfriendsservice.client.ProfileServiceClient;
import com.dsabg.dsabgfriendsservice.dto.response.FriendListProfileResponse;
import com.dsabg.dsabgfriendsservice.dto.response.PlayerSearchResponse;
import com.dsabg.dsabgfriendsservice.service.FriendshipService;
import com.dsabg.dsabgfriendsservice.service.PlayerSearchService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerSearchServiceImpl implements PlayerSearchService {

    private final ProfileServiceClient profileServiceClient;
    private final FriendshipService friendshipService;

    @Override
    public PlayerSearchResponse searchPlayerById(
            String currentUserId,
            String targetUserId) {

        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException(
                    "You cannot search for yourself");
        }

        FriendListProfileResponse profile =
                profileServiceClient.getProfileByUserId(targetUserId);

        var relationship = friendshipService.getRelationship(
                currentUserId, targetUserId);

        return new PlayerSearchResponse(
                profile,
                relationship.status());
    }
}