package com.dsabg.battleservice.dto.response;

public record PlayerMvpResponse(
        String userId,
        String displayName,
        Integer problemsSolved
) {}