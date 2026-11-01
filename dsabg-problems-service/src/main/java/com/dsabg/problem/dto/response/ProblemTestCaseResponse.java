package com.dsabg.problem.dto.response;

public record ProblemTestCaseResponse(
        Long testCaseId,
        String inputData,
        String expectedOutput,
        Boolean isPublic,
        Integer displayOrder
) {}
