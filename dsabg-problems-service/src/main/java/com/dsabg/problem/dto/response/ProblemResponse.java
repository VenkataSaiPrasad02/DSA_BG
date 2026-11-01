package com.dsabg.problem.dto.response;

import com.dsabg.problem.entity.Problem;

import java.time.LocalDateTime;
import java.util.List;

public record ProblemResponse(
        Long problemId,
        String slug,
        String title,
        String statementMd,
        Problem.Difficulty difficulty,
        String constraintsText,
        String inputFormat,
        String outputFormat,
        Problem.ProblemStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ProblemExampleResponse> examples,
        List<String> categories,
        List<PatternResponse> patterns,
        List<LanguageConfigResponse> languageConfigs
) {}