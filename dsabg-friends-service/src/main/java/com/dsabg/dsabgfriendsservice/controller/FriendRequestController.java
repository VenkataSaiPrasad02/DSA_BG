package com.dsabg.dsabgfriendsservice.controller;

import com.dsabg.dsabgfriendsservice.dto.request.SendFriendRequest;
import com.dsabg.dsabgfriendsservice.dto.response.*;
import com.dsabg.dsabgfriendsservice.service.FriendRequestService;
import com.dsabg.dsabgfriendsservice.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendRequestController {
    private final FriendRequestService friendRequestService;
    private final FriendshipService friendshipService;
    @PostMapping("/send-request")
    public FriendRequestResponse sendFriendRequest(
            @RequestBody SendFriendRequest friendRequest,
            @RequestHeader("X-User-Id")String userId
            ) {
        return friendRequestService.sendFriendRequest(friendRequest,userId);
    }

    @PatchMapping("/requests/{requestId}/accept")
    public ResponseEntity<FriendRequestResponse> acceptFriendRequest(
            @PathVariable Long requestId,
            @RequestHeader("X-User-Id") String receiverId) {

        FriendRequestResponse response =
                friendshipService.acceptFriendRequest(requestId, receiverId);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/requests/pending")
    public ResponseEntity<List<ListOfFriendRequests>> getMyPendingRequests() {
      String myId=((ServletRequestAttributes)RequestContextHolder
              .currentRequestAttributes()).getRequest().getHeader("X-User-Id");
        List<ListOfFriendRequests> requests =
                friendRequestService.getMyPendingRequests(myId);

        return ResponseEntity.status(HttpStatus.OK).body(requests);
    }
    @PatchMapping("/requests/{requestId}/reject")
    public ResponseEntity<FriendRequestResponse> rejectFriendRequest(
            @PathVariable Long requestId) {

        String receiverId = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes(),"Header unavailable"))
                .getRequest().getHeader("X-User-Id");

        return ResponseEntity.ok(
                friendshipService.rejectFriendRequest(requestId, receiverId)
        );
    }
    @GetMapping("/list")
    public List<FriendListProfileResponse> getMyFriends() {
        String userId = ((ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes(),"Header unavailable"))
                .getRequest()
                .getHeader("X-User-Id");

        return friendshipService.getMyFriends(userId);
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable String friendId) {

        String userId = ((ServletRequestAttributes)
                Objects.requireNonNull(
                        RequestContextHolder.getRequestAttributes()))
                .getRequest()
                .getHeader("X-User-Id");

        friendshipService.removeFriend(userId, friendId);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/relationship/{targetUserId}")
    public ResponseEntity<RelationshipResponse> getRelationship(
            @PathVariable String targetUserId,
            @RequestHeader("X-User-Id") String currentUserId) {

        return ResponseEntity.ok(
                friendshipService.getRelationship(
                        currentUserId, targetUserId));
    }


}
