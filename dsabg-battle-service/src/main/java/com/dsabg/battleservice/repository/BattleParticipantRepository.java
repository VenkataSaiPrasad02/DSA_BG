package com.dsabg.battleservice.repository;

import com.dsabg.battleservice.entity.BattleParticipant;
import com.dsabg.battleservice.enums.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BattleParticipantRepository extends JpaRepository<BattleParticipant, Long> {

    List<BattleParticipant> findByBattleBattleId(Long battleId);

    Optional<BattleParticipant> findByBattleBattleIdAndUserId(
            Long battleId,
            String userId
    );
    List<BattleParticipant> findByBattleBattleIdAndTeam(
            Long battleId,
            Team team
    );
}