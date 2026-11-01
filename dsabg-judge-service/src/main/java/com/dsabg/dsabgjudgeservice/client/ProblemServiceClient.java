package com.dsabg.dsabgjudgeservice.client;

import com.dsabg.dsabgjudgeservice.dto.request.TestCaseResponse;
import com.dsabg.dsabgjudgeservice.dto.response.ProblemLanguageConfigResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
@FeignClient(name = "dsabg-problem-service")
public interface ProblemServiceClient {

    @GetMapping("/problem/internal/{problemId}/test-cases")
    List<TestCaseResponse> getTestCases(
            @PathVariable("problemId") Long problemId
    );

    @GetMapping("/problem/internal/{problemId}/language-config/{language}")
    ProblemLanguageConfigResponse getLanguageConfig(
            @PathVariable("problemId") Long problemId,
            @PathVariable("language") String language
    );
}