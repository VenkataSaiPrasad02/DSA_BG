package com.dsabg.problem.service;

import com.dsabg.problem.dto.request.ProblemRequest;
import com.dsabg.problem.dto.response.ProblemResponse;
import com.dsabg.problem.dto.response.TestCaseResponse;
import com.dsabg.problem.entity.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProblemService {

    ProblemResponse getProblemBySlug(String slug);

    Page<ProblemResponse> getPublishedProblems(
            String search,
            Problem.Difficulty difficulty,
            List<String> categories,
            Pageable pageable
    );

    ProblemResponse createProblem(ProblemRequest request);

    List<TestCaseResponse> getActiveTestCases(Long problemId);

}