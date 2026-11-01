package com.dsabg.dsabgfriendsservice.client;

import com.dsabg.dsabgfriendsservice.dto.response.FriendListProfileResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProfileServiceClient {

    private final RestClient restClient;

    public ProfileServiceClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("http://localhost:8082")
                .build();
    }

    public FriendListProfileResponse getProfileByUserId(String userId) {
        return restClient.get()
                .uri("/profile/{userId}", userId)
                .retrieve()
                .body(FriendListProfileResponse.class);
    }
}