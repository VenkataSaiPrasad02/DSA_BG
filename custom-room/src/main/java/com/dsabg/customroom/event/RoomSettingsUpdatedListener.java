
package com.dsabg.customroom.event;

import com.dsabg.customroom.dto.response.RoomEvent;
import com.dsabg.customroom.entity.Room;
import com.dsabg.customroom.entity.RoomMember;
import com.dsabg.customroom.entity.enums.Difficulty;
import com.dsabg.customroom.entity.enums.Topic;
import com.dsabg.customroom.repository.RoomMemberRepository;
import com.dsabg.customroom.repository.RoomRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomSettingsUpdatedListener {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomEventPublisher roomEventPublisher;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSettingsUpdated(RoomSettingsUpdatedEvent event) {

        Room room = roomRepository.findById(event.roomCode())
                .orElse(null);

        if (room == null) {
            log.warn(
                    "Room not found while broadcasting settings: {}",
                    event.roomCode()
            );
            return;
        }

        Set<String> userIds = new HashSet<>();
        userIds.add(room.getHostUserId());

        for (RoomMember member :
                roomMemberRepository.findByRoom_RoomCode(event.roomCode())) {
            userIds.add(member.getUserId());
        }

        String message;

        try {
            message = objectMapper.writeValueAsString(
                    new SettingsPayload(
                            event.difficulty(),
                            event.topics(),
                            event.numberOfProblems()
                    )
            );
        } catch (JsonProcessingException exception) {
            log.error(
                    "Could not serialize settings for room {}",
                    event.roomCode(),
                    exception
            );
            return;
        }

        RoomEvent roomEvent = new RoomEvent(
                event.roomCode(),
                "ROOM_SETTINGS_UPDATED",
                event.updatedByUserId(),
                message
        );

        for (String userId : userIds) {
            roomEventPublisher.publishUserEvent(userId, roomEvent);
        }
    }

    private record SettingsPayload(
            Difficulty difficulty,
            Set<Topic> topics,
            Integer numberOfProblems
    ) {}
}
