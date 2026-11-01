package com.dsabg.dsabgfriendsservice.service;

import com.dsabg.dsabgfriendsservice.dto.response.PlayerSearchResponse;

public interface PlayerSearchService {

    PlayerSearchResponse searchPlayerById(
            String currentUserId,
            String targetUserId);
}