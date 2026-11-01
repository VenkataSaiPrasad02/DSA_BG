package com.dsabg.dsabgjudgeservice.dto.request;

import lombok.Data;

@Data
public class TestCaseResponse {

    private Long testCaseId;
    private String inputData;
    private String expectedOutput;
    private Integer displayOrder;
}
