package com.dsabg.battleservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AcceptedSubmissionRequest(

        @NotNull
        Long battleId,

        @NotNull
        Long problemId,

        @NotBlank
        String userId,

        @NotBlank
        String submissionId

) {}