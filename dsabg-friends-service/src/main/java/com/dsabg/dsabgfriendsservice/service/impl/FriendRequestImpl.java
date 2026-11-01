package com.dsabg.dsabgfriendsservice.service.impl;

import com.dsabg.dsabgfriendsservice.client.ProfileServiceClient;
import com.dsabg.dsabgfriendsservice.dto.request.SendFriendRequest;
import com.dsabg.dsabgfriendsservice.dto.response.FriendRequestResponse;
import com.dsabg.dsabgfriendsservice.dto.response.ListOfFriendRequests;
import com.dsabg.dsabgfriendsservice.dto.response.FriendListProfileResponse;
import com.dsabg.dsabgfriendsservice.entity.FriendRequest;
import com.dsabg.dsabgfriendsservice.entity.FriendRequestStatus;
import com.dsabg.dsabgfriendsservice.entity.Friendship;
import com.dsabg.dsabgfriendsservice.exception.SelfFriendRequestException;
import com.dsabg.dsabgfriendsservice.repository.FriendRequestRepository;
import com.dsabg.dsabgfriendsservice.repository.FriendshipRepository;
import com.dsabg.dsabgfriendsservice.service.FriendRequestService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendRequestImpl implements FriendRequestService {
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository  friendshipRepository;
    private final ProfileServiceClient profileServiceClient;

    @Override
    @Transactional
    public FriendRequestResponse sendFriendRequest(
            SendFriendRequest request,
            String senderId) {

        String receiverId = request.receiverId();

        // 1. Prevent sending a request to yourself
        if (senderId.equals(receiverId)) {
            throw new SelfFriendRequestException(
                    "You cannot add yourself as a friend");
        }

        // 2. Sort IDs to maintain canonical friendship order
        String playerOne = senderId.compareTo(receiverId) < 0
                ? senderId : receiverId;
        String playerTwo = senderId.compareTo(receiverId) < 0
                ? receiverId : senderId;

        // 3. Check whether they are already friends
        if (friendshipRepository
                .existsByPlayerOneIdAndPlayerTwoId(playerOne, playerTwo)) {
            return new FriendRequestResponse(
                    "You are already friends",
                    FriendRequestStatus.ACCEPTED);
        }

        // 4. Check for an existing request in the same direction
        if (friendRequestRepository
                .existsBySenderIdAndReceiverIdAndStatus(
                        senderId, receiverId, FriendRequestStatus.PENDING)) {
            return new FriendRequestResponse(
                    "Friend request already pending",
                    FriendRequestStatus.PENDING);
        }

        // 5. Check whether the receiver already requested the sender
        var reverseRequest = friendRequestRepository
                .findBySenderIdAndReceiverIdAndStatus(
                        receiverId, senderId, FriendRequestStatus.PENDING);

        if (reverseRequest.isPresent()) {
            FriendRequest existingRequest = reverseRequest.get();
            existingRequest.setStatus(FriendRequestStatus.ACCEPTED);
            friendRequestRepository.save(existingRequest);

            Friendship friendship = new Friendship();
            friendship.setPlayerOneId(playerOne);
            friendship.setPlayerTwoId(playerTwo);
            friendshipRepository.save(friendship);

            return new FriendRequestResponse(
                    "You are now friends",
                    FriendRequestStatus.ACCEPTED);
        }

        // 6. Create a new friend request
        FriendRequest newRequest = new FriendRequest();
        newRequest.setSenderId(senderId);
        newRequest.setReceiverId(receiverId);
        newRequest.setStatus(FriendRequestStatus.PENDING);

        friendRequestRepository.save(newRequest);

        return new FriendRequestResponse(
                "Friend request sent successfully",
                FriendRequestStatus.PENDING);
    }
    @Override
    public List<ListOfFriendRequests> getMyPendingRequests(String myId) {

        return friendRequestRepository.findByReceiverIdAndStatus(
                        myId,
                        FriendRequestStatus.PENDING
                )
                .stream()
                .map(request -> {
                    FriendListProfileResponse senderProfile =
                            profileServiceClient.getProfileByUserId(
                                    request.getSenderId()
                            );

                    return new ListOfFriendRequests(
                            request.getId(),
                            request.getSenderId(),
                            request.getStatus(),
                            senderProfile
                    );
                })
                .toList();
    }


}
