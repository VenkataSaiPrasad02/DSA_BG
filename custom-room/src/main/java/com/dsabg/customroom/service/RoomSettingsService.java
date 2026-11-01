
package com.dsabg.customroom.service;

import com.dsabg.customroom.dto.request.UpdateRoomSettingsRequest;
import com.dsabg.customroom.dto.response.RoomSettingsResponse;

public interface RoomSettingsService {

    RoomSettingsResponse updateSettings(
            Long roomCode,
            UpdateRoomSettingsRequest request,
            String userId
    );

    RoomSettingsResponse getSettings(Long roomCode, String userId);

}
