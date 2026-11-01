package com.dsabg.problem.dto.request;

public record TestCaseRequest(
        String inputData,
        String expectedOutput,
        Boolean isPublic,
        Boolean isHidden,
        Integer displayOrder
) {}
