
package com.dsabg.dsabgjudgeservice.service;

import com.dsabg.dsabgjudgeservice.dto.response.ProblemLanguageConfigResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class JavaWrapperGenerator {

    private final ObjectMapper objectMapper;

    public String generate(
            String sourceCode,
            ProblemLanguageConfigResponse config,
            String inputJson
    ) throws Exception {

        List<String> parameterNames = objectMapper.readValue(
                config.getParameterNames(),
                new TypeReference<List<String>>() {}
        );

        List<String> parameterTypes = objectMapper.readValue(
                config.getParameterTypes(),
                new TypeReference<List<String>>() {}
        );

        if (parameterNames.size() != parameterTypes.size()) {
            throw new IllegalArgumentException(
                    "Parameter names and types do not match"
            );
        }

        JsonNode input = objectMapper.readTree(inputJson);

        StringBuilder arguments = new StringBuilder();

        for (int i = 0; i < parameterNames.size(); i++) {

            String name = parameterNames.get(i);
            String type = parameterTypes.get(i);

            JsonNode value = input.get(name);

            if (value == null) {
                throw new IllegalArgumentException(
                        "Missing test input for parameter: " + name
                );
            }

            if (i > 0) {
                arguments.append(", ");
            }

            arguments.append(toJavaExpression(value, type));
        }

        String className = config.getClassName();
        String methodName = config.getMethodName();
        String returnType = config.getReturnType();

        if (!isValidIdentifier(className) ||
                !isValidIdentifier(methodName)) {
            throw new IllegalArgumentException(
                    "Invalid class or method name"
            );
        }

        String invocation = "new " + className + "()." +
                methodName + "(" + arguments + ")";

        String execution;

        if ("void".equals(returnType)) {

            execution = invocation + ";\n" +
                    "System.out.println(\"null\");";

        } else {

            execution = """
                    Object result = %s;
                    System.out.println(format(result));
                    """.formatted(invocation);
        }

        return """
                import java.util.*;
                import java.lang.reflect.Array;

                %s

                public class Main {

                    public static void main(String[] args) {

                        try {
                            %s
                        } catch (Throwable e) {
                            System.err.println(
                                e.getClass().getSimpleName()
                                + ": " + e.getMessage()
                            );
                            System.exit(1);
                        }
                    }

                    private static String format(Object value) {

                        if (value == null) {
                            return "null";
                        }

                        if (value.getClass().isArray()) {

                            int length = Array.getLength(value);
                            StringBuilder result = new StringBuilder("[");

                            for (int i = 0; i < length; i++) {

                                if (i > 0) {
                                    result.append(",");
                                }

                                result.append(format(Array.get(value, i)));
                            }

                            return result.append("]").toString();
                        }

                        if (value instanceof Collection<?> collection) {

                            StringBuilder result = new StringBuilder("[");
                            boolean first = true;

                            for (Object item : collection) {

                                if (!first) {
                                    result.append(",");
                                }

                                result.append(format(item));
                                first = false;
                            }

                            return result.append("]").toString();
                        }

                        return String.valueOf(value);
                    }
                }
                """.formatted(sourceCode, execution);
    }

    private String toJavaExpression(JsonNode value, String type) {

        if (type.endsWith("[]")) {

            if (!value.isArray()) {
                throw new IllegalArgumentException(
                        "Expected array for type: " + type
                );
            }

            String componentType =
                    type.substring(0, type.length() - 2);

            StringBuilder result = new StringBuilder(
                    "new " + componentType + "[]{"
            );

            for (int i = 0; i < value.size(); i++) {

                if (i > 0) {
                    result.append(",");
                }

                result.append(
                        toJavaExpression(value.get(i), componentType)
                );
            }

            return result.append("}").toString();
        }

        return switch (type) {

            case "int", "Integer" ->
                    requireNumber(value, type);

            case "long", "Long" ->
                    requireNumber(value, type) + "L";

            case "double", "Double" ->
                    requireNumber(value, type) + "d";

            case "float", "Float" ->
                    requireNumber(value, type) + "f";

            case "short", "Short" ->
                    "(short)" + requireNumber(value, type);

            case "byte", "Byte" ->
                    "(byte)" + requireNumber(value, type);

            case "boolean", "Boolean" -> {

                if (!value.isBoolean()) {
                    throw new IllegalArgumentException(
                            "Expected boolean"
                    );
                }

                yield value.asText();
            }

            case "String" -> {

                if (!value.isTextual()) {
                    throw new IllegalArgumentException(
                            "Expected String"
                    );
                }

                yield quoteJavaString(value.asText());
            }

            case "char", "Character" -> {

                if (!value.isTextual() ||
                        value.asText().length() != 1) {
                    throw new IllegalArgumentException(
                            "Expected single character"
                    );
                }

                yield "'" + escapeJava(value.asText()) + "'";
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported Java parameter type: " + type
            );
        };
    }

    private String requireNumber(JsonNode value, String type) {

        if (!value.isNumber()) {
            throw new IllegalArgumentException(
                    "Expected number for " + type
            );
        }

        return value.asText();
    }

    private String quoteJavaString(String value) {
        return "\"" + escapeJava(value) + "\"";
    }

    private String escapeJava(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private boolean isValidIdentifier(String value) {

        return value != null &&
                Pattern.matches(
                        "[A-Za-z_$][A-Za-z0-9_$]*",
                        value
                );
    }
}
