package com.dsabg.customroom.dto.request;

import com.dsabg.customroom.entity.enums.Team;

public record SwitchPositionRequest(
        Team team,
        Integer slotNumber
) {}