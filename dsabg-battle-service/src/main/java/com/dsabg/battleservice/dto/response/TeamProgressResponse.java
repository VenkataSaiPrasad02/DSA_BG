package com.dsabg.battleservice.dto.response;

import java.util.List;

public record TeamProgressResponse(
        String team,
        Integer totalProblems,
        Integer solvedProblems,
        List<Long> solvedProblemIds
) {}