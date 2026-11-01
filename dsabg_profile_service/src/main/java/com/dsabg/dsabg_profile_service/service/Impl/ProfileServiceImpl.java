package com.dsabg.dsabg_profile_service.service.Impl;


import com.dsabg.dsabg_profile_service.dto.request.ProfileUpdateRequest;
import com.dsabg.dsabg_profile_service.dto.response.AuthUserResponse;
import com.dsabg.dsabg_profile_service.dto.response.FriendListProfileResponse;
import com.dsabg.dsabg_profile_service.dto.response.ProfileResponse;
import com.dsabg.dsabg_profile_service.entity.Profile;
import com.dsabg.dsabg_profile_service.repository.ProfileRepository;
import com.dsabg.dsabg_profile_service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;



    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(String userId) {

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        return new ProfileResponse(
                profile.getUserId(),
                profile.getDisplayName(),
                profile.getAvatarUrl(),
                profile.getBio(),
                profile.getLevel(),
                profile.getBattlesPlayed(),
                profile.getWins(),
                profile.getLosses(),
                profile.getProblemsSolved()
        );
    }


    @Override
    @Transactional
    public ProfileResponse updateMyProfile(
            String userId,
            ProfileUpdateRequest request
    ) {

        Profile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Profile newProfile = new Profile();
                    newProfile.setUserId(userId);

                    return newProfile;
                });
        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(request.avatarUrl());
        }

        if (request.displayName() != null) {
            profile.setDisplayName(request.displayName());
        }

        if (request.bio() != null) {
            profile.setBio(request.bio());
        }

        Profile savedProfile = profileRepository.save(profile);

        return mapToResponse(savedProfile);
    }

    private ProfileResponse mapToResponse(Profile profile) {
        return new ProfileResponse(
                profile.getUserId(),
                profile.getDisplayName(),
                profile.getAvatarUrl(),
                profile.getBio(),
                profile.getLevel(),
                profile.getBattlesPlayed(),
                profile.getWins(),
                profile.getLosses(),
                profile.getProblemsSolved()
        );
    }

    @Override
    public FriendListProfileResponse getProfileByUserId(String userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profile not found"
                ));

        return new FriendListProfileResponse(
                profile.getUserId(),
                profile.getDisplayName(),
                profile.getAvatarUrl()
        );
    }
}
