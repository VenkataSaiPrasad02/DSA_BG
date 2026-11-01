package com.dsabg.problem.dto.response;

public record PatternResponse(
        Long patternId,
        String name,
        String slug,
        String description
) {}