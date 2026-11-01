package com.dsabg.problem.dto.request;

import com.dsabg.problem.entity.Problem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record ProblemRequest(
        @NotBlank(message = "Slug is required")
        @Size(max = 50, message = "Slug must be at most 50 characters")
        String slug,

        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,

        @NotBlank(message = "Statement is required")
        String statementMd,

        @NotNull(message = "Difficulty is required")
        Problem.Difficulty difficulty,

        @NotBlank(message = "Constraints text is required")
        String constraintsText,

        @NotBlank(message = "Input format is required")
        String inputFormat,

        @NotBlank(message = "Output format is required")
        String outputFormat,

        List<ExampleRequest> examples,
        Set<String> categories,
        Set<String> patterns,
        List<TestCaseRequest> testCases,
        List<LanguageConfigRequest> languageConfigs
) {}
