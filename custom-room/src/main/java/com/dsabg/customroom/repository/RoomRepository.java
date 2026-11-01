package com.dsabg.customroom.repository;

import com.dsabg.customroom.entity.Room;
import com.dsabg.customroom.entity.enums.RoomStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findById(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select room from Room room where room.roomCode = :roomCode")
    Optional<Room> findByRoomCodeForUpdate(@Param("roomCode") Long roomCode);

    List<Room> findByStatusIn(Collection<RoomStatus> status);

}
