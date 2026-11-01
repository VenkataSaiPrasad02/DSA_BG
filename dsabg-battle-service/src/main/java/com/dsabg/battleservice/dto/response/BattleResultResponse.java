package com.dsabg.battleservice.dto.response;

import com.dsabg.battleservice.enums.BattleStatus;
import com.dsabg.battleservice.enums.Team;

import java.time.LocalDateTime;
import java.util.List;

public record BattleResultResponse(
        Long battleId,
        Long roomId,
        BattleStatus status,
        Team winningTeam,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        List<TeamResultResponse> teamResults
) {}