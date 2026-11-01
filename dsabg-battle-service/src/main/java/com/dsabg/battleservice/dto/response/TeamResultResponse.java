package com.dsabg.battleservice.dto.response;

import com.dsabg.battleservice.enums.Team;

public record TeamResultResponse(
        Team team,
        Integer totalProblems,
        Integer solvedProblems,
        Long completionTimeMillis,
        PlayerMvpResponse mvp
) {}