
package com.dsabg.customroom.controller;

import com.dsabg.customroom.dto.request.UpdateRoomSettingsRequest;
import com.dsabg.customroom.dto.response.RoomSettingsResponse;
import com.dsabg.customroom.service.RoomSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room/{roomCode}/settings")
@RequiredArgsConstructor
public class RoomSettingsController {

    private final RoomSettingsService roomSettingsService;

    @PutMapping("/update-settings")
    public ResponseEntity<RoomSettingsResponse> updateSettings(
            @PathVariable Long roomCode,
            @Valid @RequestBody UpdateRoomSettingsRequest request,
            @RequestHeader("X-User-Id")String userId) {

        return ResponseEntity.ok(
                roomSettingsService.updateSettings(roomCode, request,userId)
        );
    }
    @GetMapping("/get-settings")
    public ResponseEntity<RoomSettingsResponse> getSettings(
            @PathVariable Long roomCode,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(
                roomSettingsService.getSettings(roomCode, userId)
        );
    }
}
