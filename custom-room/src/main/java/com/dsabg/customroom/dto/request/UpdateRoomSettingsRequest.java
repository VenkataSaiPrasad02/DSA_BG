package com.dsabg.customroom.dto.request;

import com.dsabg.customroom.entity.enums.Difficulty;
import com.dsabg.customroom.entity.enums.Topic;
import com.dsabg.customroom.validation.ValidProblemCount;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateRoomSettingsRequest(
        @NotNull Difficulty difficulty,
        @NotEmpty Set<@NotNull Topic> topics,
        @ValidProblemCount
        @NotNull Integer numberOfProblems
) {}