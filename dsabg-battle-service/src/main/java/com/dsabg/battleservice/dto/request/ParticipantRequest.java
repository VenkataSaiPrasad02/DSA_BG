package com.dsabg.battleservice.dto.request;

import com.dsabg.battleservice.enums.Team;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ParticipantRequest(

        @NotBlank
        String userId,

        @NotBlank
        String displayName,

        @NotNull
        Team team

) {}