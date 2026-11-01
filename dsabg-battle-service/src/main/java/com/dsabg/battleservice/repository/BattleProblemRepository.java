package com.dsabg.battleservice.repository;

import com.dsabg.battleservice.entity.BattleProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BattleProblemRepository extends JpaRepository<BattleProblem, Long> {

    List<BattleProblem> findByBattleBattleIdOrderBySequenceNumberAsc(Long battleId);

    Optional<BattleProblem> findByBattleBattleIdAndProblemId(
            Long battleId,
            Long problemId
    );
}