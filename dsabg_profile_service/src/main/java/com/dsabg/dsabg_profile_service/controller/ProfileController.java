package com.dsabg.dsabg_profile_service.controller;

import com.dsabg.dsabg_profile_service.dto.request.ProfileUpdateRequest;
import com.dsabg.dsabg_profile_service.dto.response.FriendListProfileResponse;
import com.dsabg.dsabg_profile_service.dto.response.ProfileResponse;
import com.dsabg.dsabg_profile_service.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }
    @GetMapping("/test")
    public String test() {
        return "Profile service is working";
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @RequestHeader("X-User-Id") String userId) {

        ProfileResponse profile = profileService.getMyProfile(userId);

        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @RequestHeader("X-User-Id")String userId,
            @Valid @RequestBody ProfileUpdateRequest request) {


        return ResponseEntity.ok(
                profileService.updateMyProfile(userId, request)
        );
    }
    @GetMapping("/{userId}")
    public ResponseEntity<FriendListProfileResponse> getProfileByUserId(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                profileService.getProfileByUserId(userId));
    }


}
