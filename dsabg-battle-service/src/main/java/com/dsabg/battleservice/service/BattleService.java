package com.dsabg.battleservice.service;

import com.dsabg.battleservice.dto.request.CreateBattleRequest;
import com.dsabg.battleservice.dto.response.BattleProblemResponse;
import com.dsabg.battleservice.dto.response.BattleResponse;
import com.dsabg.battleservice.dto.response.BattleResultResponse;

import java.util.List;

public interface BattleService {

    BattleResponse createBattle(CreateBattleRequest request);

    List<BattleProblemResponse> getBattleProblems(Long battleId);

    void markProblemSolved(
            Long battleId,
            Long problemId,
            String userId
    );

    BattleResultResponse getBattleResults(Long battleId);
}