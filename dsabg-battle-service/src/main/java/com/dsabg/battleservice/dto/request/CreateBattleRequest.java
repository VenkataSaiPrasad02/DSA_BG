package com.dsabg.battleservice.dto.request;

import com.dsabg.battleservice.enums.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateBattleRequest(

        @NotNull
        Long roomId,

        @NotEmpty
        List<Long> categoryIds,

        @NotNull
        Difficulty difficulty,

        @NotNull
        @Min(1)
        Integer numberOfProblems,

        @NotEmpty
        List<@Valid ParticipantRequest> participants

) {}