package com.dsabg.customroom.service.Impl;

import com.dsabg.customroom.dto.request.CreateRoomRequest;
import com.dsabg.customroom.dto.response.RoomEvent;
import com.dsabg.customroom.dto.response.RoomMemberResponse;
import com.dsabg.customroom.dto.response.RoomResponse;
import com.dsabg.customroom.entity.Room;
import com.dsabg.customroom.entity.RoomSettings;
import com.dsabg.customroom.entity.enums.RoomStatus;
import com.dsabg.customroom.event.BattleCountdownEvent;
import com.dsabg.customroom.repository.RoomMemberRepository;
import com.dsabg.customroom.repository.RoomRepository;
import com.dsabg.customroom.repository.RoomSettingsRepository;
import com.dsabg.customroom.service.RoomService;
import com.dsabg.customroom.utils.GenerateRoomCode;
import com.dsabg.customroom.utils.PasswordEncoderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.dsabg.customroom.entity.RoomMember;
import com.dsabg.customroom.entity.enums.Team;
import org.springframework.transaction.annotation.Transactional;
import com.dsabg.customroom.dto.request.SwitchPositionRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final RoomMemberRepository  roomMemberRepository;
    private final PasswordEncoderUtil passwordEncoderUtil;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RoomSettingsRepository roomSettingsRepository;

    @Override
    public List<Room> getRooms() {
        return roomRepository.findByStatusIn(
                List.of(RoomStatus.WAITING, RoomStatus.IN_BATTLE)
        );
    }

    @Override
    @Transactional
    public RoomResponse createRoom(
            CreateRoomRequest request,
            String userId,
            String displayName
    ) {
        if (roomMemberRepository.findById(userId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You must leave your current room before creating another room"
            );
        }

        Room room = new Room();

        room.setHostUserId(userId);
        room.setHostDisplayName(displayName);
        room.setRoomCode(GenerateRoomCode.generate());
        room.setRoomName(request.roomName());
        room.setStatus(RoomStatus.WAITING);

        if (request.password() != null && !request.password().isBlank()) {
            room.setPasswordHash(
                    passwordEncoderUtil.encode(request.password())
            );
        }

        // 1. Save the room first
        Room savedRoom = roomRepository.saveAndFlush(room);

        // 2. Create default settings for this room
        RoomSettings settings = new RoomSettings(savedRoom);
        roomSettingsRepository.save(settings);

        // 3. Add the host to Team A, slot 1
        roomMemberRepository.saveAndFlush(createMember(
                savedRoom,
                userId,
                displayName,
                Team.A,
                1,
                LocalDateTime.now()
        ));

        // 4. Return the room response
        return mapToRoomResponse(savedRoom);
    }


    @Override
    @Transactional
    public void joinRoom(Long roomCode, String userId,String displayName, String password) {

        // 1. Find the room
        Room room = roomRepository.findByRoomCodeForUpdate(roomCode)
                .orElseThrow(() ->
                        new RuntimeException("Room not found"));

        RoomMember existingMember = roomMemberRepository.findById(userId)
                .orElse(null);
        if (existingMember != null) {
            if (existingMember.getRoom().getRoomCode().equals(roomCode)) {
                return;
            }
            throw new RuntimeException("User is already in a room");
        }

        // 2. Only waiting rooms accept players
        if (room.getStatus() != RoomStatus.WAITING) {
            throw new RuntimeException(
                    "Room is not accepting players");
        }
        if (room.getPasswordHash() != null) {

            if (password == null || password.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Room password is required"
                );
            }

            if (!passwordEncoderUtil.matches(
                    password,
                    room.getPasswordHash())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Incorrect room password"
                );
            }
        }
        // 3. Search Team A slots 2, 3, 4
        for (int slot = 2; slot <= 4; slot++) {

            if (!roomMemberRepository
                    .existsByRoom_RoomCodeAndTeamAndSlotNumber(
                            roomCode, Team.A, slot)) {

                saveMember(room, userId,displayName, Team.A, slot);
                publishRoomUpdated(room, userId);
                return;
            }
        }

        // 5. If Team A is full, search Team B slots 1–4
        for (int slot = 1; slot <= 4; slot++) {

            if (!roomMemberRepository
                    .existsByRoom_RoomCodeAndTeamAndSlotNumber(
                            roomCode, Team.B, slot)) {

                saveMember(room, userId,displayName, Team.B, slot);
                publishRoomUpdated(room, userId);
                return;
            }
        }

        // 6. No slots available
        throw new RuntimeException("Room is full");
    }

    private void saveMember(
            Room room, String userId,String displayName, Team team, int slotNumber) {
        roomMemberRepository.save(createMember(
                room, userId, displayName, team, slotNumber, LocalDateTime.now()));
    }

    private RoomMember createMember(
            Room room,
            String userId,
            String displayName,
            Team team,
            int slotNumber,
            LocalDateTime joinedAt) {
        RoomMember member = new RoomMember();
        member.setUserId(userId);
        member.setDisplayName(displayName);
        member.setRoom(room);
        member.setTeam(team);
        member.setSlotNumber(slotNumber);
        member.setJoinedAt(joinedAt);
        return member;
    }

    private RoomMemberResponse toMemberResponse(RoomMember member) {
        return new RoomMemberResponse(
                member.getUserId(),
                member.getDisplayName(),
                member.getTeam().name(),
                member.getSlotNumber(),
                member.getJoinedAt()
        );
    }

    @Override
    @Transactional
    public void switchPosition(
            Long roomCode,
            String userId,
            SwitchPositionRequest request
    ) {
        Room room = roomRepository.findByRoomCodeForUpdate(roomCode)
                .orElseThrow(() ->
                        new RuntimeException("Room not found"));

        // Only allow position changes while the room is waiting.
        if (room.getStatus() != RoomStatus.WAITING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Positions cannot be changed after the battle starts"
            );
        }

        boolean isHost = room.getHostUserId().equals(userId);
        RoomMember member = roomMemberRepository
                .findByRoom_RoomCodeAndUserId(roomCode, userId)
                .orElse(null);
        if (!isHost && member == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not a member of this room"
            );
        }

        ensureHostMember(room);
        if (member == null) {
            member = roomMemberRepository
                    .findByRoom_RoomCodeAndUserId(roomCode, userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "You are not a member of this room"
                    ));
        }

        // Validate the requested position.
        Team targetTeam = request.team();
        Integer targetSlot = request.slotNumber();

        if (targetTeam == null
                || targetSlot == null
                || targetSlot < 1
                || targetSlot > 4) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid team or slot number"
            );
        }

        // Nothing to change if the user selected their current position.
        if (member.getTeam() == targetTeam
                && member.getSlotNumber().equals(targetSlot)) {
            return;
        }

        // Reject a slot occupied by another member.
        boolean occupied = roomMemberRepository
                .existsByRoom_RoomCodeAndTeamAndSlotNumber(
                        roomCode,
                        targetTeam,
                        targetSlot
                );

        if (occupied) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "That slot is already occupied"
            );
        }

        // Update the member's position.
        member.setTeam(targetTeam);
        member.setSlotNumber(targetSlot);

        roomMemberRepository.save(member);
        publishRoomUpdated(room, userId);
    }


    @Override
    @Transactional
    public RoomResponse getRoomDetails(Long roomCode, String userId) {

        Room room = roomRepository.findById(roomCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Room not found"
                ));

        boolean isHost = room.getHostUserId().equals(userId);

        boolean isMember = roomMemberRepository
                .existsByRoom_RoomCodeAndUserId(roomCode, userId);

        if (!isHost && !isMember) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the room host or its members can view room details"
            );
        }

        if (roomMemberRepository
                .findByRoom_RoomCodeAndUserId(roomCode, room.getHostUserId())
                .isEmpty()) {
            room = roomRepository.findByRoomCodeForUpdate(roomCode)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Room not found"
                    ));
            ensureHostMember(room);
        }
        return mapToRoomResponse(room);
    }

    private void ensureHostMember(Room room) {
        String hostUserId = room.getHostUserId();
        RoomMember existingHost = roomMemberRepository.findById(hostUserId)
                .orElse(null);
        if (existingHost != null) {
            if (existingHost.getRoom().getRoomCode().equals(room.getRoomCode())) {
                return;
            }
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The room host is already assigned to another room and needs data repair"
            );
        }

        if (roomMemberRepository.existsByRoom_RoomCodeAndTeamAndSlotNumber(
                room.getRoomCode(), Team.A, 1)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Team A, Slot 1 is occupied; this room needs data repair"
            );
        }

        LocalDateTime joinedAt = room.getCreatedAt() != null
                ? room.getCreatedAt()
                : LocalDateTime.now();
        roomMemberRepository.save(createMember(
                room,
                hostUserId,
                room.getHostDisplayName(),
                Team.A,
                1,
                joinedAt
        ));
    }

    private RoomResponse mapToRoomResponse(Room room) {
        List<RoomMemberResponse> roomMembers =
                roomMemberRepository
                        .findByRoomRoomCode(room.getRoomCode())
                        .stream()
                        .map(this::toMemberResponse)
                        .toList();

        return new RoomResponse(
                room.getRoomCode(),
                room.getRoomName(),
                room.getStatus(),
                room.getHostUserId(),
                room.getHostDisplayName(),
                room.getCreatedAt(),
                roomMembers
        );
    }
    @Override
    @Transactional
    public void leaveRoom(Long roomCode, String userId) {

        // 1. Find the room
        Room room = roomRepository.findById(roomCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Room not found"
                ));

        if (room.getStatus() == RoomStatus.IN_BATTLE) {
            RoomMember member = roomMemberRepository
                    .findByRoom_RoomCodeAndUserId(roomCode, userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "You are not a participant in this battle"
                    ));
            roomMemberRepository.delete(member);
            publishRoomUpdated(room, userId);
            return;
        }

        // 2. If the host leaves, delete the entire room
        if (room.getHostUserId().equals(userId)) {

            // Delete all members belonging to this room first
            List<RoomMember> members =
                    roomMemberRepository.findByRoomRoomCode(roomCode);

            publishRoomClosed(room, members);
            roomMemberRepository.deleteAll(members);
            roomSettingsRepository.deleteById(roomCode);
            // Flush dependent rows, including settings topics, before deleting the room.
            roomSettingsRepository.flush();

            // Delete the room
            roomRepository.delete(room);

            return;
        }

        // 3. Otherwise, find the member
        RoomMember member = roomMemberRepository
                .findByRoom_RoomCodeAndUserId(roomCode, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not the host or a member of this room"
                ));

        // 4. Delete only this member
        roomMemberRepository.delete(member);
        publishRoomUpdated(room, userId);
    }

    @Override
    @Transactional
    public void kickMember(Long roomCode, String hostUserId, String targetUserId) {

        // 1. Find the room
        Room room = roomRepository.findById(roomCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Room not found"
                ));

        // 2. Verify that the caller is the host
        if (!room.getHostUserId().equals(hostUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the host can kick members"
            );
        }

        // 3. Prevent the host from kicking themselves
        if (hostUserId.equals(targetUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Host cannot kick themselves"
            );
        }

        // 4. Find the target member in this room
        RoomMember member = roomMemberRepository
                .findByRoom_RoomCodeAndUserId(roomCode, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Member not found in this room"
                ));

        // 5. Remove only the target member
        roomMemberRepository.delete(member);

        applicationEventPublisher.publishEvent(
                new RoomEvent(
                        roomCode,
                        "PLAYER_KICKED",
                        targetUserId,
                        "Host removed you from the room"
                )
        );
        publishRoomUpdated(room, hostUserId);
    }

    private void publishRoomUpdated(Room room, String excludedUserId) {
        Set<String> userIds = new HashSet<>();
        userIds.add(room.getHostUserId());
        roomMemberRepository.findByRoom_RoomCode(room.getRoomCode())
                .forEach(member -> userIds.add(member.getUserId()));
        userIds.remove(excludedUserId);

        userIds.forEach(userId -> applicationEventPublisher.publishEvent(
                new RoomEvent(room.getRoomCode(), "ROOM_UPDATED", userId, null)
        ));
    }

    private void publishRoomClosed(Room room, List<RoomMember> members) {
        Set<String> userIds = new HashSet<>();
        userIds.add(room.getHostUserId());
        members.forEach(member -> userIds.add(member.getUserId()));

        userIds.forEach(userId -> applicationEventPublisher.publishEvent(
                new RoomEvent(room.getRoomCode(), "ROOM_CLOSED", userId, null)
        ));
    }
    @Override
    @Transactional
    public void startBattle(Long roomCode, String userId) {
        Room room = roomRepository.findById(roomCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found"
                ));

        if (!room.getHostUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the host can start the battle"
            );
        }

        if (room.getStatus() != RoomStatus.WAITING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Room is not waiting for a battle"
            );
        }

        room.setStatus(RoomStatus.COUNTDOWN);

        applicationEventPublisher.publishEvent(
                new BattleCountdownEvent(roomCode)
        );
    }
}
