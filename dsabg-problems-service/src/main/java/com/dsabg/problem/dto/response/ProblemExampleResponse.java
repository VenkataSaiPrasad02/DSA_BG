package com.dsabg.problem.dto.response;

public record ProblemExampleResponse(
        Long exampleId,
        String inputData,
        String expectedOutput,
        String explanation,
        Integer displayOrder
) {}