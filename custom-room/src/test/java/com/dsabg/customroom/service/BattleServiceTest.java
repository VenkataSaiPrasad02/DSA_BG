package com.dsabg.customroom.service;

import com.dsabg.customroom.dto.response.RoomEvent;
import com.dsabg.customroom.entity.Room;
import com.dsabg.customroom.entity.RoomMember;
import com.dsabg.customroom.entity.enums.RoomStatus;
import com.dsabg.customroom.repository.RoomMemberRepository;
import com.dsabg.customroom.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BattleServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMemberRepository roomMemberRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void transitioningToBattleNotifiesRoomParticipantsToResynchronize() {
        Room room = new Room();
        room.setRoomCode(12345L);
        room.setHostUserId("room-host");
        room.setStatus(RoomStatus.COUNTDOWN);
        RoomMember member = new RoomMember();
        member.setUserId("player-1");

        when(roomRepository.findById(12345L)).thenReturn(Optional.of(room));
        when(roomMemberRepository.findByRoom_RoomCode(12345L))
                .thenReturn(List.of(member));

        new BattleService(
                roomRepository,
                roomMemberRepository,
                applicationEventPublisher
        ).transitionToBattle(12345L);

        assertEquals(RoomStatus.IN_BATTLE, room.getStatus());
        ArgumentCaptor<RoomEvent> eventCaptor = ArgumentCaptor.forClass(RoomEvent.class);
        verify(applicationEventPublisher, org.mockito.Mockito.times(2))
                .publishEvent(eventCaptor.capture());
        assertEquals(
                Set.of("room-host", "player-1"),
                eventCaptor.getAllValues().stream()
                        .map(RoomEvent::userId)
                        .collect(java.util.stream.Collectors.toSet())
        );
        eventCaptor.getAllValues().forEach(event ->
                assertEquals("ROOM_UPDATED", event.eventType()));
    }
}
