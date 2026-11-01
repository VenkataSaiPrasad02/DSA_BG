
package com.dsabg.customroom.event;

import com.dsabg.customroom.entity.enums.Difficulty;
import com.dsabg.customroom.entity.enums.Topic;

import java.util.Set;

public record RoomSettingsUpdatedEvent(
        Long roomCode,
        String updatedByUserId,
        Difficulty difficulty,
        Set<Topic> topics,
        Integer numberOfProblems
) {
    public RoomSettingsUpdatedEvent {
        topics = Set.copyOf(topics);
    }
}
