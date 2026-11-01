
package com.dsabg.customroom.service.Impl;

import com.dsabg.customroom.dto.request.UpdateRoomSettingsRequest;
import com.dsabg.customroom.dto.response.RoomSettingsResponse;
import com.dsabg.customroom.entity.Room;
import com.dsabg.customroom.entity.RoomSettings;
import com.dsabg.customroom.entity.enums.RoomStatus;
import com.dsabg.customroom.event.RoomSettingsUpdatedEvent;
import com.dsabg.customroom.repository.RoomMemberRepository;
import com.dsabg.customroom.repository.RoomRepository;
import com.dsabg.customroom.repository.RoomSettingsRepository;
import com.dsabg.customroom.service.RoomSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomSettingsServiceImpl implements RoomSettingsService {

    private final RoomRepository roomRepository;
    private final RoomSettingsRepository roomSettingsRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    @Transactional
    public RoomSettingsResponse updateSettings(
            Long roomCode,
            UpdateRoomSettingsRequest request,
            String userId) {

        Room room = roomRepository.findById(roomCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Room not found"));

        if (!room.getHostUserId().equals(userId)) {
            throw new AccessDeniedException(
                    "Only the room host can update settings");
        }

        if (room.getStatus() != RoomStatus.WAITING) {
            throw new IllegalStateException(
                    "Settings can only be changed while the room is waiting");
        }

        RoomSettings settings = roomSettingsRepository.findById(roomCode)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Settings not found for this room"));

        settings.setDifficulty(request.difficulty());
        settings.setTopics(new HashSet<>(request.topics()));
        settings.setNumberOfProblems(request.numberOfProblems());

        RoomSettings saved = roomSettingsRepository.save(settings);

        applicationEventPublisher.publishEvent(
                new RoomSettingsUpdatedEvent(
                        roomCode,
                        userId,
                        saved.getDifficulty(),
                        Set.copyOf(saved.getTopics()),
                        saved.getNumberOfProblems()
                )
        );

        return new RoomSettingsResponse(
                saved.getRoomCode(),
                saved.getDifficulty(),
                Set.copyOf(saved.getTopics()),
                saved.getNumberOfProblems()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RoomSettingsResponse getSettings(Long roomCode, String userId) {

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
                    "Only room members can view room settings"
            );
        }

        RoomSettings settings = roomSettingsRepository.findById(roomCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Room settings not found"
                ));

        return new RoomSettingsResponse(
                settings.getRoomCode(),
                settings.getDifficulty(),
                Set.copyOf(settings.getTopics()),
                settings.getNumberOfProblems()
        );
    }
}
