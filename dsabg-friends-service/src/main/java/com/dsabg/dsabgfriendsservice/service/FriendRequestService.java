package com.dsabg.dsabgfriendsservice.service;

import com.dsabg.dsabgfriendsservice.dto.request.SendFriendRequest;
import com.dsabg.dsabgfriendsservice.dto.response.FriendRequestResponse;
import com.dsabg.dsabgfriendsservice.dto.response.ListOfFriendRequests;


import java.util.List;

public interface FriendRequestService {
    FriendRequestResponse sendFriendRequest(SendFriendRequest friendRequest,String userId);

    List<ListOfFriendRequests> getMyPendingRequests(String myId);
}
