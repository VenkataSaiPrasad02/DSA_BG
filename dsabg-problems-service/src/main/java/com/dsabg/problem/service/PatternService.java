package com.dsabg.problem.service;

import com.dsabg.problem.dto.request.PatternRequest;
import com.dsabg.problem.entity.Pattern;

import java.util.List;
import java.util.Set;

public interface PatternService {

    Pattern createPattern(PatternRequest patternRequest);

    List<Pattern> getPatternsByCategoryIds(
            List<Long> categoryIds
    );

    void assignCategories(
            Long patternId,
            Set<Long> categoryIds
    );
}