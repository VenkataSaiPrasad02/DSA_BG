package com.dsabg.customroom.service.Impl;

import com.dsabg.customroom.entity.Room;
import com.dsabg.customroom.entity.RoomMember;
import com.dsabg.customroom.entity.enums.RoomStatus;
import com.dsabg.customroom.entity.enums.Team;
import com.dsabg.customroom.dto.request.CreateRoomRequest;
import com.dsabg.customroom.dto.request.SwitchPositionRequest;
import com.dsabg.customroom.dto.response.RoomEvent;
import com.dsabg.customroom.dto.response.RoomResponse;
import com.dsabg.customroom.repository.RoomMemberRepository;
import com.dsabg.customroom.repository.RoomRepository;
import com.dsabg.customroom.repository.RoomSettingsRepository;
import com.dsabg.customroom.utils.PasswordEncoderUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMemberRepository roomMemberRepository;

    @Mock
    private RoomSettingsRepository roomSettingsRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PasswordEncoderUtil passwordEncoderUtil;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Room requestedRoom;

    @BeforeEach
    void setUp() {
        requestedRoom = new Room();
        requestedRoom.setRoomCode(12345L);
        requestedRoom.setStatus(RoomStatus.WAITING);
    }

    @Test
    void hostLeavingActiveBattleRemovesOnlyTheirMembership() {
        requestedRoom.setHostUserId("room-host");
        requestedRoom.setStatus(RoomStatus.IN_BATTLE);
        RoomMember host = new RoomMember();
        host.setUserId("room-host");
        host.setRoom(requestedRoom);
        when(roomRepository.findById(12345L)).thenReturn(Optional.of(requestedRoom));
        when(roomMemberRepository.findByRoom_RoomCodeAndUserId(12345L, "room-host"))
                .thenReturn(Optional.of(host));

        roomService.leaveRoom(12345L, "room-host");

        verify(roomMemberRepository).delete(host);
        verify(roomRepository, never()).delete(any(Room.class));
        verify(roomMemberRepository, never()).deleteAll(any());
    }

    @Test
    void joiningPlayerNotifiesExistingRoomParticipants() {
        requestedRoom.setHostUserId("room-host");
        RoomMember host = new RoomMember();
        host.setUserId("room-host");
        host.setRoom(requestedRoom);
        when(roomRepository.findByRoomCodeForUpdate(12345L))
                .thenReturn(Optional.of(requestedRoom));
        when(roomMemberRepository.findById("new-player")).thenReturn(Optional.empty());
        when(roomMemberRepository.existsByRoom_RoomCodeAndTeamAndSlotNumber(
                12345L, Team.A, 2)).thenReturn(false);
        when(roomMemberRepository.findByRoom_RoomCode(12345L))
                .thenReturn(java.util.List.of(host));

        roomService.joinRoom(12345L, "new-player", "New Player", null);

        ArgumentCaptor<RoomEvent> eventCaptor = ArgumentCaptor.forClass(RoomEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(12345L, eventCaptor.getValue().roomCode());
        assertEquals("ROOM_UPDATED", eventCaptor.getValue().eventType());
        assertEquals("room-host", eventCaptor.getValue().userId());
    }

    @Test
    void regularMemberLeavingWhileOthersRemainKeepsRoomAndSettings() {
        requestedRoom.setHostUserId("room-host");
        RoomMember leavingMember = new RoomMember();
        leavingMember.setUserId("player-leaving");
        leavingMember.setRoom(requestedRoom);
        RoomMember remainingMember = new RoomMember();
        remainingMember.setUserId("player-remaining");
        remainingMember.setRoom(requestedRoom);
        when(roomRepository.findById(12345L)).thenReturn(Optional.of(requestedRoom));
        when(roomMemberRepository.findByRoom_RoomCodeAndUserId(12345L, "player-leaving"))
                .thenReturn(Optional.of(leavingMember));

        roomService.leaveRoom(12345L, "player-leaving");

        verify(roomMemberRepository).delete(leavingMember);
        verify(roomMemberRepository, never()).delete(remainingMember);
        verify(roomMemberRepository, never()).deleteAll(any());
        verify(roomRepository, never()).delete(any(Room.class));
        verify(roomSettingsRepository, never()).deleteById(12345L);
        verify(roomSettingsRepository, never()).flush();
    }

    @Test
    void lastRegularMemberLeavingKeepsHostRoomAndSettings() {
        requestedRoom.setHostUserId("room-host");
        RoomMember lastMember = new RoomMember();
        lastMember.setUserId("last-member");
        lastMember.setRoom(requestedRoom);
        when(roomRepository.findById(12345L)).thenReturn(Optional.of(requestedRoom));
        when(roomMemberRepository.findByRoom_RoomCodeAndUserId(12345L, "last-member"))
                .thenReturn(Optional.of(lastMember));

        roomService.leaveRoom(12345L, "last-member");

        verify(roomMemberRepository).delete(lastMember);
        verify(roomRepository, never()).delete(any(Room.class));
        verify(roomSettingsRepository, never()).deleteById(12345L);
    }

    @Test
    void lastPlayerHostLeavingClosesRoomAndDeletesSettings() {
        requestedRoom.setHostUserId("room-host");
        when(roomRepository.findById(12345L)).thenReturn(Optional.of(requestedRoom));
        when(roomMemberRepository.findByRoomRoomCode(12345L))
                .thenReturn(java.util.List.of());

        roomService.leaveRoom(12345L, "room-host");

        InOrder cleanupOrder = org.mockito.Mockito.inOrder(
                roomMemberRepository,
                roomSettingsRepository,
                roomRepository
        );
        cleanupOrder.verify(roomMemberRepository).deleteAll(java.util.List.of());
        cleanupOrder.verify(roomSettingsRepository).deleteById(12345L);
        cleanupOrder.verify(roomSettingsRepository).flush();
        cleanupOrder.verify(roomRepository).delete(requestedRoom);
    }

    @Test
    void hostLeavingWithOtherMembersRemovesMembershipsBeforeRoomAndSettings() {
        requestedRoom.setHostUserId("room-host");
        RoomMember host = new RoomMember();
        host.setUserId("room-host");
        host.setRoom(requestedRoom);
        RoomMember player = new RoomMember();
        player.setUserId("player-1");
        player.setRoom(requestedRoom);
        when(roomRepository.findById(12345L)).thenReturn(Optional.of(requestedRoom));
        when(roomMemberRepository.findByRoomRoomCode(12345L))
                .thenReturn(java.util.List.of(host, player));

        roomService.leaveRoom(12345L, "room-host");

        InOrder cleanupOrder = org.mockito.Mockito.inOrder(
                roomMemberRepository,
                roomSettingsRepository,
                roomRepository
        );
        cleanupOrder.verify(roomMemberRepository).deleteAll(java.util.List.of(host, player));
        cleanupOrder.verify(roomSettingsRepository).deleteById(12345L);
        cleanupOrder.verify(roomSettingsRepository).flush();
        cleanupOrder.verify(roomRepository).delete(requestedRoom);
    }

    @Test
    void joinRoomIsIdempotentWhenUserIsAlreadyInRequestedRoom() {
        requestedRoom.setStatus(RoomStatus.IN_BATTLE);
        RoomMember existingMember = new RoomMember();
        existingMember.setUserId("player-1");
        existingMember.setRoom(requestedRoom);
        when(roomMemberRepository.findById("player-1"))
                .thenReturn(Optional.of(existingMember));
        when(roomRepository.findByRoomCodeForUpdate(12345L))
                .thenReturn(Optional.of(requestedRoom));

        assertDoesNotThrow(() ->
                roomService.joinRoom(12345L, "player-1", "Player One", null));

        verify(roomMemberRepository, never()).save(any(RoomMember.class));
        verify(roomMemberRepository, never()).existsById("player-1");
    }

    @Test
    void joinRoomStillRejectsUserWhoBelongsToAnotherRoom() {
        Room otherRoom = new Room();
        otherRoom.setRoomCode(67890L);
        RoomMember existingMember = new RoomMember();
        existingMember.setUserId("player-1");
        existingMember.setRoom(otherRoom);
        when(roomMemberRepository.findById("player-1"))
                .thenReturn(Optional.of(existingMember));
        when(roomRepository.findByRoomCodeForUpdate(12345L))
                .thenReturn(Optional.of(requestedRoom));

        assertThrows(RuntimeException.class, () ->
                roomService.joinRoom(12345L, "player-1", "Player One", null));

        verify(roomMemberRepository, never()).save(any(RoomMember.class));
    }

    @Test
    void roomDetailsRemainForbiddenForNonMembersAndNonHosts() {
        requestedRoom.setHostUserId("room-host");
        when(roomRepository.findById(12345L)).thenReturn(Optional.of(requestedRoom));
        when(roomMemberRepository.existsByRoom_RoomCodeAndUserId(12345L, "stranger"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> roomService.getRoomDetails(12345L, "stranger")
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );
    }

    @Test
    void hostCanRestoreRoomDetailsWithoutAMemberRecord() {
        requestedRoom.setHostUserId("room-host");
        requestedRoom.setHostDisplayName("Room Host");
        when(roomRepository.findById(12345L)).thenReturn(Optional.of(requestedRoom));
        when(roomRepository.findByRoomCodeForUpdate(12345L))
                .thenReturn(Optional.of(requestedRoom));
        when(roomMemberRepository.findByRoom_RoomCodeAndUserId(12345L, "room-host"))
                .thenReturn(Optional.empty());
        when(roomMemberRepository.findById("room-host")).thenReturn(Optional.empty());
        when(roomMemberRepository.existsByRoom_RoomCodeAndTeamAndSlotNumber(
                12345L, Team.A, 1)).thenReturn(false);
        java.util.concurrent.atomic.AtomicReference<RoomMember> persistedHost =
                new java.util.concurrent.atomic.AtomicReference<>();
        when(roomMemberRepository.save(any(RoomMember.class))).thenAnswer(invocation -> {
            RoomMember member = invocation.getArgument(0);
            persistedHost.set(member);
            return member;
        });
        when(roomMemberRepository.findByRoomRoomCode(12345L)).thenAnswer(invocation ->
                persistedHost.get() == null
                        ? java.util.List.of()
                        : java.util.List.of(persistedHost.get()));

        RoomResponse response = roomService.getRoomDetails(12345L, "room-host");

        org.junit.jupiter.api.Assertions.assertEquals(12345L, response.roomCode());
        org.junit.jupiter.api.Assertions.assertEquals("room-host", response.hostUserId());
        assertEquals(1, response.roomMembers().size());
        assertEquals("room-host", response.roomMembers().get(0).userId());
        assertEquals("A", response.roomMembers().get(0).team());
        assertEquals(1, response.roomMembers().get(0).slotNumber());
        verify(roomMemberRepository).save(any(RoomMember.class));
        verifyNoInteractions(passwordEncoderUtil);
    }

    @Test
    void roomDetailsDoNotSilentlyOmitHostAssignedToAnotherRoom() {
        requestedRoom.setHostUserId("room-host");
        Room otherRoom = new Room();
        otherRoom.setRoomCode(67890L);
        RoomMember hostInOtherRoom = new RoomMember();
        hostInOtherRoom.setUserId("room-host");
        hostInOtherRoom.setRoom(otherRoom);
        when(roomRepository.findById(12345L)).thenReturn(Optional.of(requestedRoom));
        when(roomMemberRepository.existsByRoom_RoomCodeAndUserId(12345L, "room-host"))
                .thenReturn(false);
        when(roomMemberRepository.findByRoom_RoomCodeAndUserId(12345L, "room-host"))
                .thenReturn(Optional.empty());
        when(roomRepository.findByRoomCodeForUpdate(12345L))
                .thenReturn(Optional.of(requestedRoom));
        when(roomMemberRepository.findById("room-host"))
                .thenReturn(Optional.of(hostInOtherRoom));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> roomService.getRoomDetails(12345L, "room-host")
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void createRoomPersistsHostInTeamASlotOneAndReturnsRoster() {
        when(roomMemberRepository.findById("room-host")).thenReturn(Optional.empty());
        when(roomRepository.saveAndFlush(any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        java.util.concurrent.atomic.AtomicReference<RoomMember> persistedHost =
                new java.util.concurrent.atomic.AtomicReference<>();
        when(roomMemberRepository.saveAndFlush(any(RoomMember.class))).thenAnswer(invocation -> {
            RoomMember member = invocation.getArgument(0);
            persistedHost.set(member);
            return member;
        });
        when(roomMemberRepository.findByRoomRoomCode(any()))
                .thenAnswer(invocation -> java.util.List.of(persistedHost.get()));

        RoomResponse response = roomService.createRoom(
                new CreateRoomRequest("Room", null), "room-host", "Room Host");

        assertEquals(1, response.roomMembers().size());
        assertEquals("room-host", response.roomMembers().get(0).userId());
        assertEquals("Room Host", response.roomMembers().get(0).displayName());
        assertEquals("A", response.roomMembers().get(0).team());
        assertEquals(1, response.roomMembers().get(0).slotNumber());
        verify(roomMemberRepository).saveAndFlush(any(RoomMember.class));
    }

    @Test
    void hostCanSwitchToAnAvailableSlot() {
        requestedRoom.setHostUserId("room-host");
        RoomMember host = new RoomMember();
        host.setUserId("room-host");
        host.setRoom(requestedRoom);
        host.setTeam(Team.A);
        host.setSlotNumber(1);
        when(roomRepository.findByRoomCodeForUpdate(12345L))
                .thenReturn(Optional.of(requestedRoom));
        when(roomMemberRepository.findById("room-host")).thenReturn(Optional.of(host));
        when(roomMemberRepository.findByRoom_RoomCodeAndUserId(12345L, "room-host"))
                .thenReturn(Optional.of(host));
        when(roomMemberRepository.existsByRoom_RoomCodeAndTeamAndSlotNumber(
                12345L, Team.B, 2)).thenReturn(false);

        roomService.switchPosition(12345L, "room-host",
                new SwitchPositionRequest(Team.B, 2));

        assertEquals(Team.B, host.getTeam());
        assertEquals(2, host.getSlotNumber());
        verify(roomMemberRepository).save(host);
    }

    @Test
    void switchPositionRejectsAnOccupiedDestinationWithoutSaving() {
        requestedRoom.setHostUserId("room-host");
        RoomMember host = new RoomMember();
        host.setUserId("room-host");
        host.setRoom(requestedRoom);
        host.setTeam(Team.A);
        host.setSlotNumber(1);
        when(roomRepository.findByRoomCodeForUpdate(12345L))
                .thenReturn(Optional.of(requestedRoom));
        when(roomMemberRepository.findById("room-host")).thenReturn(Optional.of(host));
        when(roomMemberRepository.findByRoom_RoomCodeAndUserId(12345L, "room-host"))
                .thenReturn(Optional.of(host));
        when(roomMemberRepository.existsByRoom_RoomCodeAndTeamAndSlotNumber(
                12345L, Team.B, 2)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> roomService.switchPosition(12345L, "room-host",
                        new SwitchPositionRequest(Team.B, 2))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(Team.A, host.getTeam());
        assertEquals(1, host.getSlotNumber());
        verify(roomMemberRepository, never()).save(host);
    }
}
