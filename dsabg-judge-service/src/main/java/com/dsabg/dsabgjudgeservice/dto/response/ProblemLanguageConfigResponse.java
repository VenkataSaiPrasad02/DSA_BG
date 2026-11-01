package com.dsabg.dsabgjudgeservice.dto.response;

import lombok.Data;

@Data
public class ProblemLanguageConfigResponse {

    private String className;
    private String language;
    private String methodName;
    private String parameterNames;
    private String parameterTypes;
    private String returnType;
    private String starterCode;
}