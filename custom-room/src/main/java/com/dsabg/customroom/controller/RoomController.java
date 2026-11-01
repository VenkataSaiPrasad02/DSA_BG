package com.dsabg.customroom.controller;

import com.dsabg.customroom.dto.request.CreateRoomRequest;
import com.dsabg.customroom.dto.request.JoinRoomRequest;
import com.dsabg.customroom.dto.request.SwitchPositionRequest;
import com.dsabg.customroom.dto.response.RoomResponse;
import com.dsabg.customroom.entity.Room;
import com.dsabg.customroom.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/get-all-rooms")
    public List<Room> getRooms() {
        return roomService.getRooms();
    }

    @PostMapping("/create")

    public ResponseEntity<RoomResponse> createRoom(
            @RequestBody CreateRoomRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String displayName) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roomService.createRoom(request,userId,displayName));
    }

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    public void joinRoom(
            @RequestBody JoinRoomRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String displayName) {

        roomService.joinRoom(
                request.roomCode(),
                userId,
                displayName,
                request.password()
        );
    }
    @GetMapping("/{roomCode}/get-your-room-details")
    public ResponseEntity<RoomResponse> getRoomDetails(
            @PathVariable Long roomCode,
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(
                roomService.getRoomDetails(roomCode, userId)
        );
    }
    @PatchMapping("/{roomCode}/switch-position")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void switchPosition(
            @PathVariable Long roomCode,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody SwitchPositionRequest request
    ) {
        roomService.switchPosition(roomCode, userId, request);
    }
    @DeleteMapping("/{roomCode}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveRoom(
            @PathVariable Long roomCode,
            @RequestHeader("X-User-Id") String userId) {

        roomService.leaveRoom(roomCode, userId);
    }
    @DeleteMapping("/{roomCode}/members/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kickMember(
            @PathVariable Long roomCode,
            @PathVariable String targetUserId,
            @RequestHeader("X-User-Id") String hostUserId) {

        roomService.kickMember(roomCode, hostUserId, targetUserId);
    }

    @PostMapping("/{roomCode}/start")
    public ResponseEntity<Void> startBattle(
            @PathVariable Long roomCode,
            @RequestHeader("X-User-Id") String userId) {

        roomService.startBattle(roomCode, userId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
