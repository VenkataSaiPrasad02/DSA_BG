package com.dsabg.problem.service;

import com.dsabg.problem.dto.response.ProblemLanguageConfigResponse;

public interface ProblemLanguageService {
    public ProblemLanguageConfigResponse getLanguageConfig(
            Long problemId, String language);
}
