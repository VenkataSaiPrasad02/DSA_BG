package com.dsabg.problem.dto.response;

import lombok.Data;

@Data
public class ProblemLanguageConfigResponse {

    private String language;
    private String className;
    private String methodName;
    private String returnType;
    private String parameterTypes;
    private String parameterNames;
    private String starterCode;
}