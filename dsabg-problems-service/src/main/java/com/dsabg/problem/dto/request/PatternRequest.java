package com.dsabg.problem.dto.request;

import java.util.Set;

public record PatternRequest(
        String name,
        String slug,
        String description,
        Set<Long> categoryIds
) {
}