package com.dsabg.customroom.event;

import com.dsabg.customroom.dto.response.RoomEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RoomEventListener {

    private final RoomEventPublisher roomEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomEvent(RoomEvent event) {
        roomEventPublisher.publishUserEvent(
                event.userId(),
                event
        );
    }
}