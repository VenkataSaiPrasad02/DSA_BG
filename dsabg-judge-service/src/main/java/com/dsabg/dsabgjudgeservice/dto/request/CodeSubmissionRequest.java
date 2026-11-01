package com.dsabg.dsabgjudgeservice.dto.request;

import lombok.Data;

@Data
public class CodeSubmissionRequest {

    private Long problemId;

    private String language;

    private String sourceCode;
}