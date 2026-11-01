package com.dsabg.customroom.dto.response;

import com.dsabg.customroom.entity.enums.Difficulty;
import com.dsabg.customroom.entity.enums.Topic;

import java.util.Set;

public record RoomSettingsResponse(
        Long roomCode,
        Difficulty difficulty,
        Set<Topic> topics,
        Integer numberOfProblems
) {}