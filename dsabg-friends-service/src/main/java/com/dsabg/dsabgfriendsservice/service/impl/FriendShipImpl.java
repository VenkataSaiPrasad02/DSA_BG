package com.dsabg.dsabgfriendsservice.service.impl;

import com.dsabg.dsabgfriendsservice.client.ProfileServiceClient;
import com.dsabg.dsabgfriendsservice.dto.response.FriendListProfileResponse;
import com.dsabg.dsabgfriendsservice.dto.response.RelationshipResponse;
import com.dsabg.dsabgfriendsservice.entity.FriendShipStatus;
import com.dsabg.dsabgfriendsservice.exception.SelfFriendRequestException;
import com.dsabg.dsabgfriendsservice.repository.FriendRequestRepository;
import com.dsabg.dsabgfriendsservice.repository.FriendshipRepository;
import com.dsabg.dsabgfriendsservice.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dsabg.dsabgfriendsservice.entity.FriendRequest;
import com.dsabg.dsabgfriendsservice.entity.Friendship;
import com.dsabg.dsabgfriendsservice.entity.FriendRequestStatus;
import com.dsabg.dsabgfriendsservice.dto.response.FriendRequestResponse;

import java.util.List;


@Service
@RequiredArgsConstructor
public class FriendShipImpl implements FriendshipService {
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final ProfileServiceClient  profileServiceClient;

    @Override
    @Transactional
    public FriendRequestResponse acceptFriendRequest(
            Long requestId, String receiverId) {

        // 1. Find the friend request
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException("Friend request not found"));

        // 2. Only the intended receiver can accept it
        if (!request.getReceiverId().equals(receiverId)) {
            throw new RuntimeException(
                    "You are not allowed to accept this request");
        }

        // 3. Only pending requests can be accepted
        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new RuntimeException(
                    "Friend request is not pending");
        }

        // 4. Update request status
        request.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);

        // 5. Store friendship using canonical ID order
        String senderId = request.getSenderId();

        String playerOneId;
        String playerTwoId;

        if (senderId.compareTo(receiverId) < 0) {
            playerOneId = senderId;
            playerTwoId = receiverId;
        } else {
            playerOneId = receiverId;
            playerTwoId = senderId;
        }

        Friendship friendship = new Friendship();
        friendship.setPlayerOneId(playerOneId);
        friendship.setPlayerTwoId(playerTwoId);

        friendshipRepository.save(friendship);

        // 6. Return response
        return new FriendRequestResponse(
                "Friend request accepted",
                FriendRequestStatus.ACCEPTED);
    }

    @Override
    @Transactional
    public FriendRequestResponse rejectFriendRequest(Long requestId, String receiverId) {

        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!request.getReceiverId().equals(receiverId)) {
            throw new RuntimeException("You are not allowed to reject this request");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new RuntimeException("Friend request is not pending");
        }

        request.setStatus(FriendRequestStatus.REJECTED);
        friendRequestRepository.save(request);

        return new FriendRequestResponse(
                "Friend request rejected",
                FriendRequestStatus.REJECTED
        );
    }
    @Override
    public List<FriendListProfileResponse> getMyFriends(String userId) {

        return friendshipRepository
                .findByPlayerOneIdOrPlayerTwoId(userId, userId)
                .stream()
                .map(friendship -> {
                    String friendId = friendship.getPlayerOneId().equals(userId)
                            ? friendship.getPlayerTwoId()
                            : friendship.getPlayerOneId();

                    return profileServiceClient.getProfileByUserId(friendId);
                })
                .toList();
    }

    @Override
    @Transactional
    public void removeFriend(String userId, String friendId) {

        String playerOne = userId.compareTo(friendId) < 0
                ? userId : friendId;

        String playerTwo = userId.compareTo(friendId) < 0
                ? friendId : userId;

        Friendship friendship = friendshipRepository
                .findByPlayerOneIdAndPlayerTwoId(playerOne, playerTwo)
                .orElseThrow(() ->
                        new RuntimeException("Friendship not found"));

        friendshipRepository.delete(friendship);
    }

    @Override
    public RelationshipResponse getRelationship(
            String currentUserId,
            String targetUserId) {

        if (currentUserId.equals(targetUserId)) {
            throw new SelfFriendRequestException(
                    "You cannot check a relationship with yourself");
        }

        String playerOne = currentUserId.compareTo(targetUserId) < 0
                ? currentUserId : targetUserId;

        String playerTwo = currentUserId.compareTo(targetUserId) < 0
                ? targetUserId : currentUserId;

        if (friendshipRepository.existsByPlayerOneIdAndPlayerTwoId(
                playerOne, playerTwo)) {
            return new RelationshipResponse(
                    targetUserId, FriendShipStatus.CONNECTED);
        }

        return new RelationshipResponse(
                targetUserId, FriendShipStatus.NOT_CONNECTED);
    }
}
