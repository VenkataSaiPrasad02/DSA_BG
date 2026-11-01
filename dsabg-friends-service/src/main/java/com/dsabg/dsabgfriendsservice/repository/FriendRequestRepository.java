package com.dsabg.dsabgfriendsservice.repository;

import com.dsabg.dsabgfriendsservice.dto.response.ListOfFriendRequests;
import com.dsabg.dsabgfriendsservice.entity.FriendRequest;
import com.dsabg.dsabgfriendsservice.entity.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository
        extends JpaRepository<FriendRequest, Long> {

    boolean existsBySenderIdAndReceiverIdAndStatus(
            String senderId,
            String receiverId,
            FriendRequestStatus status
    );

    Optional<FriendRequest> findBySenderIdAndReceiverIdAndStatus(
            String senderId,
            String receiverId,
            FriendRequestStatus status
    );

    List<FriendRequest> findByReceiverIdAndStatus(
            String receiverId,
            FriendRequestStatus status
    );
    boolean existsBySenderIdAndReceiverIdAndStatusOrSenderIdAndReceiverIdAndStatus(
            String senderId,
            String receiverId,
            FriendRequestStatus firstStatus,
            String reverseSenderId,
            String reverseReceiverId,
            FriendRequestStatus secondStatus
    );
}