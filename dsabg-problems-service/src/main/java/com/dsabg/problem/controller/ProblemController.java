package com.dsabg.problem.controller;

import com.dsabg.problem.dto.request.ProblemRequest;
import com.dsabg.problem.dto.response.ProblemResponse;
import com.dsabg.problem.dto.response.TestCaseResponse;
import com.dsabg.problem.entity.Problem;
import com.dsabg.problem.repository.ProblemRepository;
import com.dsabg.problem.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/problem")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;
    private final ProblemRepository problemRepository;

    @GetMapping("/{slug}")
    public ProblemResponse getProblemBySlug(@PathVariable String slug) {
        return problemService.getProblemBySlug(slug);
    }

    @GetMapping
    public ResponseEntity<Page<ProblemResponse>> getPublishedProblems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Problem.Difficulty difficulty,
            @RequestParam(required = false) List<String> categories,
            @PageableDefault(sort = "problemId", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                problemService.getPublishedProblems(
                        search, difficulty, categories, pageable
                )
        );
    }

    @PostMapping
    public ResponseEntity<ProblemResponse> createProblem(@Valid @RequestBody ProblemRequest request) {
        ProblemResponse response = problemService.createProblem(request);
        return ResponseEntity.status(201).body(response);
    }
    @GetMapping("/internal/eligible")
    public List<Long> getEligibleProblemIds(
            @RequestParam List<Long> categoryIds,
            @RequestParam Problem.Difficulty difficulty
    ) {
        return problemRepository.findEligibleProblemIds(
                categoryIds, difficulty
        );
    }

    @GetMapping("/internal/{problemId}/test-cases")
    public List<TestCaseResponse> getTestCases(
            @PathVariable Long problemId) {

        return problemService.getActiveTestCases(problemId);
    }
}