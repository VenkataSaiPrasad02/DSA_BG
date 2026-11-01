package com.dsabg.customroom.repository;


import com.dsabg.customroom.entity.RoomMember;
import com.dsabg.customroom.entity.enums.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, String> {


    boolean existsByRoom_RoomCodeAndTeamAndSlotNumber(
            Long roomCode, Team team, Integer slotNumber);

    Optional<RoomMember> findByRoom_RoomCodeAndUserId(
            Long roomCode,
            String userId
    );

    boolean existsByRoom_RoomCodeAndUserId(Long roomCode, String userId);

    List<RoomMember> findByRoomRoomCode(Long roomCode);

    List<RoomMember> findByRoom_RoomCode(Long roomCode);
}
