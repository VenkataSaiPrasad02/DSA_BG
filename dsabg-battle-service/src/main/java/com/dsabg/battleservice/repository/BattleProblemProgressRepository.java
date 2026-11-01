package com.dsabg.battleservice.repository;

import com.dsabg.battleservice.entity.BattleProblemProgress;
import com.dsabg.battleservice.enums.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BattleProblemProgressRepository extends JpaRepository<BattleProblemProgress, Long> {

    List<BattleProblemProgress> findByBattleProblemBattleBattleIdAndTeam(
            Long battleId, Team team);

    Optional<BattleProblemProgress> findByBattleProblemIdAndTeam(
            Long battleProblemId, Team team);


}