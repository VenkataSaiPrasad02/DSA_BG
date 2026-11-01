package com.dsabg.problem.dto.request;

public record ExampleRequest(
        String inputData,
        String expectedOutput,
        String explanation,
        Integer displayOrder
) {}
