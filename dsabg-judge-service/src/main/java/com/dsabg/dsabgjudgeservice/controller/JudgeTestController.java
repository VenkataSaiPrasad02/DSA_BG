package com.dsabg.dsabgjudgeservice.controller;

import com.dsabg.dsabgjudgeservice.client.ProblemServiceClient;
import com.dsabg.dsabgjudgeservice.dto.request.TestCaseResponse;
import com.dsabg.dsabgjudgeservice.dto.response.ProblemLanguageConfigResponse;
import com.dsabg.dsabgjudgeservice.service.JavaWrapperGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/judge/test")
@RequiredArgsConstructor
public class JudgeTestController {

    private final ProblemServiceClient problemServiceClient;
    private final JavaWrapperGenerator javaWrapperGenerator;

    @PostMapping("/{problemId}/generate/{language}")
    public String generate(
            @PathVariable Long problemId,
            @PathVariable String language,
            @RequestBody String sourceCode) throws Exception {

        ProblemLanguageConfigResponse config =
                problemServiceClient.getLanguageConfig(problemId, language);

        List<TestCaseResponse> testCases =
                problemServiceClient.getTestCases(problemId);

        if (testCases.isEmpty()) {
            throw new IllegalStateException("No test cases found");
        }

        return javaWrapperGenerator.generate(
                sourceCode,
                config,
                testCases.getFirst().getInputData()
        );
    }

    @GetMapping("/{problemId}/test-cases")
    public List<TestCaseResponse> getTestCases(
            @PathVariable Long problemId) {

        return problemServiceClient.getTestCases(problemId);
    }
}