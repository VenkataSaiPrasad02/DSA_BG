package com.dsabg.dsabgjudgeservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Judge0SubmissionResponse {

    private String token;

    private String stdout;

    private String stderr;

    @JsonProperty("compile_output")
    private String compileOutput;

    private String message;

    private String time;

    private Integer memory;

    private Status status;

    @Data
    public static class Status {

        private Integer id;

        private String description;
    }
}