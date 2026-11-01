package com.dsabg.dsabg_profile_service.dto.request;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(

        @Size(max = 1000, message = "Avatar URL must not exceed 1000 characters")
        String avatarUrl,

        @Size(max = 100, message = "Display name must not exceed 100 characters")
        String displayName,

        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio

) {
}
