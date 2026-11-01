package com.dsabg.customroom.service;

import com.dsabg.customroom.dto.request.CreateRoomRequest;
import com.dsabg.customroom.dto.request.SwitchPositionRequest;
import com.dsabg.customroom.dto.response.RoomResponse;
import com.dsabg.customroom.entity.Room;

import java.util.List;

public interface RoomService {

    List<Room> getRooms();

    RoomResponse createRoom(CreateRoomRequest request,String userId,String displayName);

    void joinRoom(Long roomCode,String userId,String displayName,String password);

    void switchPosition(
            Long roomCode,
            String userId,
            SwitchPositionRequest request
    );

    RoomResponse getRoomDetails(Long roomCode, String userId);

    void leaveRoom(Long roomCode, String userId);

    void kickMember(Long roomCode, String hostUserId, String targetUserId);

    void startBattle(Long roomCode, String userId);

}
