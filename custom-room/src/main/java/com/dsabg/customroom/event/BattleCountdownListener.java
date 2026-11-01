
package com.dsabg.customroom.event;

import com.dsabg.customroom.dto.response.RoomEvent;
import com.dsabg.customroom.entity.Room;
import com.dsabg.customroom.entity.RoomMember;
import com.dsabg.customroom.entity.enums.RoomStatus;
import com.dsabg.customroom.repository.RoomMemberRepository;
import com.dsabg.customroom.repository.RoomRepository;
import com.dsabg.customroom.service.BattleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BattleCountdownListener {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomEventPublisher roomEventPublisher;
    private final ThreadPoolTaskScheduler battleTaskScheduler;
    private final BattleService battleService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBattleCountdown(BattleCountdownEvent event) {

        Long roomCode = event.roomCode();

        Room room = roomRepository.findById(roomCode).orElse(null);

        if (room == null || room.getStatus() != RoomStatus.COUNTDOWN) {
            return;
        }

        Instant countdownStart = Instant.now();
        String message = countdownStart.toString();

        Set<String> userIds = new HashSet<>();
        userIds.add(room.getHostUserId());

        for (RoomMember member :
                roomMemberRepository.findByRoom_RoomCode(roomCode)) {
            userIds.add(member.getUserId());
        }

        for (String userId : userIds) {
            RoomEvent roomEvent = new RoomEvent(
                    roomCode,
                    "BATTLE_COUNTDOWN",
                    userId,
                    message
            );

            roomEventPublisher.publishUserEvent(userId, roomEvent);
        }

        battleTaskScheduler.schedule(
                () -> battleService.transitionToBattle(roomCode),
                countdownStart.plusSeconds(3)
        );
    }


}
