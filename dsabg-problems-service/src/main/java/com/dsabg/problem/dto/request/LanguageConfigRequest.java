package com.dsabg.problem.dto.request;

public record LanguageConfigRequest(
        String language,
        String className,
        String methodName,
        String returnType,
        String parameterTypes,
        String parameterNames,
        String starterCode
) {}
