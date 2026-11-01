package com.dsabg.battleservice.dto.response;

import java.time.LocalDateTime;

public record BattleResponse(
        Long battleId,
        Long roomId,
        Integer battleNumber,
        Integer totalProblems,
        String status,
        LocalDateTime startedAt
) {}