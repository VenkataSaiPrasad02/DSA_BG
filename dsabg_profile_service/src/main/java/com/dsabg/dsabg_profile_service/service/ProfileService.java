package com.dsabg.dsabg_profile_service.service;

import com.dsabg.dsabg_profile_service.dto.response.FriendListProfileResponse;
import com.dsabg.dsabg_profile_service.dto.response.ProfileResponse;
import com.dsabg.dsabg_profile_service.dto.request.ProfileUpdateRequest;

public interface ProfileService {

    ProfileResponse getMyProfile(String userId);

    ProfileResponse updateMyProfile(
            String userId,
            ProfileUpdateRequest request
    );
    FriendListProfileResponse getProfileByUserId(String userId);

}
