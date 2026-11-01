package com.dsabg.dsabgjudgeservice.dto.request;

import lombok.Data;

@Data
public class Judge0SubmissionRequest {

    private String source_code;

    private Integer language_id;

    private String stdin;

    private String expected_output;

    private Boolean wait;

    private Boolean base64_encoded;
}