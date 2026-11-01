package com.dsabg.customroom.event;

import com.dsabg.customroom.dto.response.RoomEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public void publishUserEvent(String userId, RoomEvent roomEvent) {
        log.info(
                "Publishing room event roomCode={} eventType={} recipientUserId={}",
                roomEvent.roomCode(),
                roomEvent.eventType(),
                userId
        );
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/room-events",
                roomEvent
                );
    }
}
