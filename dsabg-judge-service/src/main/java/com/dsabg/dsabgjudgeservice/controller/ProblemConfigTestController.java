package com.dsabg.dsabgjudgeservice.controller;

import com.dsabg.dsabgjudgeservice.client.ProblemServiceClient;
import com.dsabg.dsabgjudgeservice.dto.response.ProblemLanguageConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/judge/test")
@RequiredArgsConstructor
public class ProblemConfigTestController {

    private final ProblemServiceClient problemServiceClient;

    @GetMapping("/{problemId}/config/{language}")
    public ProblemLanguageConfigResponse getConfig(
            @PathVariable Long problemId,
            @PathVariable String language) {

        return problemServiceClient.getLanguageConfig(problemId, language);
    }
}