package com.dsabg.problem.service.impl;

import com.dsabg.problem.dto.response.ProblemLanguageConfigResponse;
import com.dsabg.problem.entity.ProblemLanguageConfig;
import com.dsabg.problem.repository.ProblemLanguageConfigRepository;

import com.dsabg.problem.service.ProblemLanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProblemLanguageConfigImpl
        implements ProblemLanguageService {

    private final ProblemLanguageConfigRepository problemLanguageConfigRepository;

    @Override
    public ProblemLanguageConfigResponse getLanguageConfig(
            Long problemId, String language) {

        ProblemLanguageConfig config = problemLanguageConfigRepository
                .findByProblem_ProblemIdAndLanguageIgnoreCase(
                        problemId, language)
                .orElseThrow(() ->
                        new RuntimeException("Language configuration not found"));

        ProblemLanguageConfigResponse response = new ProblemLanguageConfigResponse();
        response.setLanguage(config.getLanguage());
        response.setClassName(config.getClassName());
        response.setMethodName(config.getMethodName());
        response.setReturnType(config.getReturnType());
        response.setParameterTypes(config.getParameterTypes());
        response.setParameterNames(config.getParameterNames());
        response.setStarterCode(config.getStarterCode());
        return response;
    }
}