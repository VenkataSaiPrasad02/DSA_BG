package com.dsabg.problem.dto.response;

public record LanguageConfigResponse(
        Long configId,
        String language,
        String className,
        String methodName,
        String returnType,
        String parameterTypes,
        String parameterNames,
        String starterCode
) {}