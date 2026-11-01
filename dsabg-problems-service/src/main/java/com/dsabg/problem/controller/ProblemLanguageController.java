package com.dsabg.problem.controller;

import com.dsabg.problem.dto.response.ProblemLanguageConfigResponse;
import com.dsabg.problem.entity.ProblemLanguageConfig;
import com.dsabg.problem.service.ProblemLanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problem")
public class ProblemLanguageController {
 private final ProblemLanguageService problemLanguageService;
    @GetMapping("/internal/{problemId}/language-config/{language}")
    public ProblemLanguageConfigResponse getLanguageConfig(
            @PathVariable Long problemId,
            @PathVariable String language) {

        return problemLanguageService.getLanguageConfig(problemId, language);
    }
}
