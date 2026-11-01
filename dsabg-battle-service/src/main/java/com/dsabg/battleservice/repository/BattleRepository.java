package com.dsabg.battleservice.repository;

import com.dsabg.battleservice.entity.Battle;
import com.dsabg.battleservice.enums.BattleStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BattleRepository extends JpaRepository<Battle, Long> {

    Optional<Battle> findByRoomIdAndBattleNumber(Long roomId, Integer battleNumber);

    Optional<Battle> findFirstByRoomIdOrderByBattleNumberDesc(Long roomId);

    boolean existsByRoomIdAndStatus(Long roomId, BattleStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Battle b WHERE b.battleId = :battleId")
    Optional<Battle> findByIdForUpdate(@Param("battleId") Long battleId);
}