package com.dsabg.problem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestCaseResponse {

    private Long testCaseId;
    private String inputData;
    private String expectedOutput;
    private Integer displayOrder;
}