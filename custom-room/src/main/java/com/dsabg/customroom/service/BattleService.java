package com.dsabg.customroom.service;

import com.dsabg.customroom.dto.response.RoomEvent;
import com.dsabg.customroom.entity.Room;
import com.dsabg.customroom.entity.enums.RoomStatus;
import com.dsabg.customroom.repository.RoomMemberRepository;
import com.dsabg.customroom.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BattleService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void transitionToBattle(Long roomCode) {
        Room room = roomRepository.findById(roomCode)
                .orElse(null);

        if (room == null ||
                room.getStatus() != RoomStatus.COUNTDOWN) {
            return;
        }

        room.setStatus(RoomStatus.IN_BATTLE);

        Set<String> userIds = new HashSet<>();
        userIds.add(room.getHostUserId());
        roomMemberRepository.findByRoom_RoomCode(roomCode)
                .forEach(member -> userIds.add(member.getUserId()));

        userIds.forEach(userId -> applicationEventPublisher.publishEvent(
                new RoomEvent(roomCode, "ROOM_UPDATED", userId, null)
        ));
    }
}