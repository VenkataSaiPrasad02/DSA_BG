
package com.dsabg.problem.service.impl;

import com.dsabg.problem.dto.request.ExampleRequest;
import com.dsabg.problem.dto.request.LanguageConfigRequest;
import com.dsabg.problem.dto.request.ProblemRequest;
import com.dsabg.problem.dto.request.TestCaseRequest;
import com.dsabg.problem.dto.response.*;
import com.dsabg.problem.entity.Category;
import com.dsabg.problem.entity.Pattern;
import com.dsabg.problem.entity.Problem;
import com.dsabg.problem.entity.ProblemExample;
import com.dsabg.problem.entity.ProblemLanguageConfig;
import com.dsabg.problem.entity.ProblemTestCase;
import com.dsabg.problem.exception.ProblemNotFoundException;
import com.dsabg.problem.repository.CategoryRepository;
import com.dsabg.problem.repository.PatternRepository;
import com.dsabg.problem.repository.ProblemExampleRepository;
import com.dsabg.problem.repository.ProblemLanguageConfigRepository;
import com.dsabg.problem.repository.ProblemRepository;
import com.dsabg.problem.repository.ProblemTestCaseRepository;
import com.dsabg.problem.service.ProblemService;
import com.dsabg.problem.specification.ProblemSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemExampleRepository problemExampleRepository;
    private final ProblemTestCaseRepository problemTestCaseRepository;
    private final ProblemLanguageConfigRepository problemLanguageConfigRepository;
    private final PatternRepository patternRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public ProblemResponse getProblemBySlug(String slug) {

        Problem problem = problemRepository.findBySlug(slug)
                .orElseThrow(() -> new ProblemNotFoundException(slug));

        List<ProblemExampleResponse> examples =
                problemExampleRepository
                        .findByProblemProblemIdOrderByDisplayOrderAsc(
                                problem.getProblemId()
                        )
                        .stream()
                        .map(example -> new ProblemExampleResponse(
                                example.getExampleId(),
                                example.getInputData(),
                                example.getExpectedOutput(),
                                example.getExplanation(),
                                example.getDisplayOrder()
                        ))
                        .toList();

        List<String> categories = problem.getCategories()
                .stream()
                .map(Category::getName)
                .sorted()
                .toList();

        List<PatternResponse> patterns =
                patternRepository.findPatternsByProblemIds(
                                List.of(problem.getProblemId())
                        )
                        .stream()
                        .map(row -> new PatternResponse(
                                (Long) row[1],
                                (String) row[2],
                                (String) row[3],
                                (String) row[4]
                        ))
                        .toList();

        List<LanguageConfigResponse> languageConfigs =
                problemLanguageConfigRepository
                        .findByProblem_ProblemId(problem.getProblemId())
                        .stream()
                        .map(config -> new LanguageConfigResponse(
                                config.getConfigId(),
                                config.getLanguage(),
                                config.getClassName(),
                                config.getMethodName(),
                                config.getReturnType(),
                                config.getParameterTypes(),
                                config.getParameterNames(),
                                config.getStarterCode()
                        ))
                        .toList();

        return new ProblemResponse(
                problem.getProblemId(),
                problem.getSlug(),
                problem.getTitle(),
                problem.getStatementMd(),
                problem.getDifficulty(),
                problem.getConstraintsText(),
                problem.getInputFormat(),
                problem.getOutputFormat(),
                problem.getStatus(),
                problem.getCreatedAt(),
                problem.getUpdatedAt(),
                examples,
                categories,
                patterns,
                languageConfigs
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProblemResponse> getPublishedProblems(
            String search,
            Problem.Difficulty difficulty,
            List<String> categories,
            Pageable pageable
    ) {

        Specification<Problem> spec =
                ProblemSpecification.published()
                        .and(ProblemSpecification.search(search))
                        .and(ProblemSpecification.difficulty(difficulty))
                        .and(ProblemSpecification.categories(categories));

        // 1. Fetch filtered problems with pagination
        Page<Problem> problems = problemRepository.findAll(spec, pageable);

        // 2. Extract problem IDs
        List<Long> problemIds = problems.getContent()
                .stream()
                .map(Problem::getProblemId)
                .toList();

        // 3. Fetch categories in bulk
        List<Object[]> categoryRows = problemIds.isEmpty()
                ? List.of()
                : problemRepository.findCategoriesByProblemId(problemIds);

        // 4. Group categories by problem ID
        Map<Long, List<String>> categoriesMap = new HashMap<>();

        for (Object[] row : categoryRows) {
            Long problemId = (Long) row[0];
            String categoryName = (String) row[1];

            categoriesMap
                    .computeIfAbsent(problemId, key -> new ArrayList<>())
                    .add(categoryName);
        }

        // 5. Fetch examples in bulk
        List<ProblemExample> exampleRows = problemIds.isEmpty()
                ? List.of()
                : problemExampleRepository.findByProblemProblemIdIn(problemIds);

        // 6. Group examples by problem ID
        Map<Long, List<ProblemExampleResponse>> examplesMap = new HashMap<>();

        for (ProblemExample example : exampleRows) {

            Long problemId = example.getProblem().getProblemId();

            ProblemExampleResponse response = new ProblemExampleResponse(
                    example.getExampleId(),
                    example.getInputData(),
                    example.getExpectedOutput(),
                    example.getExplanation(),
                    example.getDisplayOrder()
            );

            examplesMap
                    .computeIfAbsent(problemId, key -> new ArrayList<>())
                    .add(response);
        }

        // 7. Fetch patterns in bulk
        List<Object[]> patternRows = problemIds.isEmpty()
                ? List.of()
                : patternRepository.findPatternsByProblemIds(problemIds);

        // 8. Group patterns by problem ID
        Map<Long, List<PatternResponse>> patternsMap = new HashMap<>();

        for (Object[] row : patternRows) {

            Long problemId = (Long) row[0];

            PatternResponse response = new PatternResponse(
                    (Long) row[1],
                    (String) row[2],
                    (String) row[3],
                    (String) row[4]
            );

            patternsMap
                    .computeIfAbsent(problemId, key -> new ArrayList<>())
                    .add(response);
        }

        // 9. Fetch language configs in bulk
        List<ProblemLanguageConfig> langConfigRows = problemIds.isEmpty()
                ? List.of()
                : problemLanguageConfigRepository.findByProblemProblemIdIn(problemIds);

        // 10. Group language configs by problem ID
        Map<Long, List<LanguageConfigResponse>> langConfigsMap = new HashMap<>();

        for (ProblemLanguageConfig config : langConfigRows) {
            Long problemId = config.getProblem().getProblemId();

            LanguageConfigResponse response = new LanguageConfigResponse(
                    config.getConfigId(),
                    config.getLanguage(),
                    config.getClassName(),
                    config.getMethodName(),
                    config.getReturnType(),
                    config.getParameterTypes(),
                    config.getParameterNames(),
                    config.getStarterCode()
            );

            langConfigsMap
                    .computeIfAbsent(problemId, key -> new ArrayList<>())
                    .add(response);
        }

        // 11. Convert problems into response objects
        return problems.map(problem -> new ProblemResponse(
                problem.getProblemId(),
                problem.getSlug(),
                problem.getTitle(),
                problem.getStatementMd(),
                problem.getDifficulty(),
                problem.getConstraintsText(),
                problem.getInputFormat(),
                problem.getOutputFormat(),
                problem.getStatus(),
                problem.getCreatedAt(),
                problem.getUpdatedAt(),

                examplesMap.getOrDefault(
                        problem.getProblemId(),
                        List.of()
                ),

                categoriesMap.getOrDefault(
                        problem.getProblemId(),
                        List.of()
                ),

                patternsMap.getOrDefault(
                        problem.getProblemId(),
                        List.of()
                ),

                langConfigsMap.getOrDefault(
                        problem.getProblemId(),
                        List.of()
                )
        ));
    }

    @Override
    @Transactional
    public ProblemResponse createProblem(ProblemRequest request) {

        // 0. Validate slug uniqueness
        if (problemRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("Problem with slug already exists: " + request.slug());
        }

        // 1. Create the problem entity
        Problem problem = new Problem();
        problem.setSlug(request.slug());
        problem.setTitle(request.title());
        problem.setStatementMd(request.statementMd());
        problem.setDifficulty(request.difficulty());
        problem.setConstraintsText(request.constraintsText());
        problem.setInputFormat(request.inputFormat());
        problem.setOutputFormat(request.outputFormat());
        problem.setStatus(Problem.ProblemStatus.DRAFT);
        LocalDateTime now = LocalDateTime.now();
        problem.setCreatedAt(now);
        problem.setUpdatedAt(now);

        problem = problemRepository.save(problem);

        // 2. Save examples
        List<ProblemExampleResponse> exampleResponses = new ArrayList<>();
        if (request.examples() != null) {
            for (ExampleRequest ex : request.examples()) {
                ProblemExample example = new ProblemExample();
                example.setProblem(problem);
                example.setInputData(ex.inputData());
                example.setExpectedOutput(ex.expectedOutput());
                example.setExplanation(ex.explanation());
                example.setDisplayOrder(ex.displayOrder() != null ? ex.displayOrder() : 1);
                example = problemExampleRepository.save(example);

                exampleResponses.add(new ProblemExampleResponse(
                        example.getExampleId(),
                        example.getInputData(),
                        example.getExpectedOutput(),
                        example.getExplanation(),
                        example.getDisplayOrder()
                ));
            }
        }

        // 3. Save test cases
        if (request.testCases() != null) {
            for (TestCaseRequest tc : request.testCases()) {
                ProblemTestCase testCase = new ProblemTestCase();
                testCase.setProblem(problem);
                testCase.setInputData(tc.inputData());
                testCase.setExpectedOutput(tc.expectedOutput());
                testCase.setIsPublic(tc.isPublic() != null ? tc.isPublic() : false);
                testCase.setIsHidden(tc.isHidden() != null ? tc.isHidden() : true);
                testCase.setIsActive(true);
                testCase.setDisplayOrder(tc.displayOrder() != null ? tc.displayOrder() : 1);
                testCase.setCreatedAt(now);
                testCase.setUpdatedAt(now);
                problemTestCaseRepository.save(testCase);
            }
        }

        // 4. Save language configurations
        List<LanguageConfigResponse> languageConfigResponses = new ArrayList<>();
        if (request.languageConfigs() != null) {
            for (LanguageConfigRequest lc : request.languageConfigs()) {
                ProblemLanguageConfig config = new ProblemLanguageConfig();
                config.setProblem(problem);
                config.setLanguage(lc.language());
                config.setClassName(lc.className());
                config.setMethodName(lc.methodName());
                config.setReturnType(lc.returnType());
                config.setParameterTypes(lc.parameterTypes());
                config.setParameterNames(lc.parameterNames());
                config.setStarterCode(lc.starterCode());
                config = problemLanguageConfigRepository.save(config);

                languageConfigResponses.add(new LanguageConfigResponse(
                        config.getConfigId(),
                        config.getLanguage(),
                        config.getClassName(),
                        config.getMethodName(),
                        config.getReturnType(),
                        config.getParameterTypes(),
                        config.getParameterNames(),
                        config.getStarterCode()
                ));
            }
        }

        // 5. Resolve and assign categories
        Set<Category> categories = new HashSet<>();
        if (request.categories() != null) {
            for (String categoryName : request.categories()) {
                Category category = categoryRepository.findByName(categoryName)
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setName(categoryName);
                            return categoryRepository.save(newCategory);
                        });
                categories.add(category);
            }
        }
        problem.setCategories(categories);

        // 6. Resolve and assign patterns
        Set<Pattern> patterns = new HashSet<>();
        if (request.patterns() != null) {
            for (String patternSlug : request.patterns()) {
                Pattern pattern = patternRepository.findBySlug(patternSlug)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Pattern not found: " + patternSlug));
                patterns.add(pattern);
                pattern.getProblems().add(problem);
            }
        }
        problem.setPatterns(patterns);

        problemRepository.save(problem);

        // 7. Build and return the response
        List<String> categoryNames = problem.getCategories()
                .stream()
                .map(Category::getName)
                .sorted()
                .toList();

        List<PatternResponse> patternResponses =
                patternRepository.findPatternsByProblemIds(
                                List.of(problem.getProblemId())
                        )
                        .stream()
                        .map(row -> new PatternResponse(
                                (Long) row[1],
                                (String) row[2],
                                (String) row[3],
                                (String) row[4]
                        ))
                        .toList();

        return new ProblemResponse(
                problem.getProblemId(),
                problem.getSlug(),
                problem.getTitle(),
                problem.getStatementMd(),
                problem.getDifficulty(),
                problem.getConstraintsText(),
                problem.getInputFormat(),
                problem.getOutputFormat(),
                problem.getStatus(),
                problem.getCreatedAt(),
                problem.getUpdatedAt(),
                exampleResponses,
                categoryNames,
                patternResponses,
                languageConfigResponses
        );
    }

    public List<TestCaseResponse> getActiveTestCases(Long problemId) {

        return problemTestCaseRepository
                .findByProblem_ProblemIdAndIsActiveTrueOrderByDisplayOrderAsc(problemId)
                .stream()
                .map(testCase -> new TestCaseResponse(
                        testCase.getTestCaseId(),
                        testCase.getInputData(),
                        testCase.getExpectedOutput(),
                        testCase.getDisplayOrder()
                ))
                .toList();
    }
}
