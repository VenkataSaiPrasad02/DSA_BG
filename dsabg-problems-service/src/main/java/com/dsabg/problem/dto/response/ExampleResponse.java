package com.dsabg.problem.dto.response;

public record ExampleResponse(
        Long exampleId,
         String inputData,
         String expectedOutput,
         String explanation,
         Integer displayOrder
) {
}
