package com.dsabg.problem.service.impl;

import com.dsabg.problem.dto.request.ExampleRequest;
import com.dsabg.problem.dto.request.LanguageConfigRequest;
import com.dsabg.problem.dto.request.ProblemRequest;
import com.dsabg.problem.dto.request.TestCaseRequest;
import com.dsabg.problem.dto.response.LanguageConfigResponse;
import com.dsabg.problem.dto.response.ProblemResponse;
import com.dsabg.problem.entity.Category;
import com.dsabg.problem.entity.Pattern;
import com.dsabg.problem.entity.Problem;
import com.dsabg.problem.entity.ProblemExample;
import com.dsabg.problem.entity.ProblemLanguageConfig;
import com.dsabg.problem.entity.ProblemTestCase;
import com.dsabg.problem.repository.CategoryRepository;
import com.dsabg.problem.repository.PatternRepository;
import com.dsabg.problem.repository.ProblemExampleRepository;
import com.dsabg.problem.repository.ProblemLanguageConfigRepository;
import com.dsabg.problem.repository.ProblemRepository;
import com.dsabg.problem.repository.ProblemTestCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceImplCreateTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private ProblemExampleRepository problemExampleRepository;

    @Mock
    private ProblemTestCaseRepository problemTestCaseRepository;

    @Mock
    private ProblemLanguageConfigRepository problemLanguageConfigRepository;

    @Mock
    private PatternRepository patternRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProblemServiceImpl problemServiceImpl;

    private Problem makeProblem(Long id, String slug, String title, String statementMd,
                                Problem.Difficulty difficulty, String constraints,
                                String inputFmt, String outputFmt) {
        Problem p = new Problem();
        p.setProblemId(id);
        p.setSlug(slug);
        p.setTitle(title);
        p.setStatementMd(statementMd);
        p.setDifficulty(difficulty);
        p.setConstraintsText(constraints);
        p.setInputFormat(inputFmt);
        p.setOutputFormat(outputFmt);
        p.setStatus(Problem.ProblemStatus.DRAFT);
        LocalDateTime now = LocalDateTime.now();
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        p.setCategories(new java.util.HashSet<>());
        p.setPatterns(new java.util.HashSet<>());
        return p;
    }

    private Pattern makePattern(Long id, String slug) {
        Pattern p = new Pattern();
        p.setPatternId(id);
        p.setName(slug);
        p.setSlug(slug);
        p.setDescription("Description for " + slug);
        return p;
    }

    private Category makeCategory(Long id, String name) {
        Category c = new Category();
        c.setCategoryId(id);
        c.setName(name);
        return c;
    }

    @Test
    void createProblem_withAllFields_shouldCreateProblemWithExamplesAndTestCases() {
        // Arrange
        Problem savedProblem = makeProblem(1L, "two-sum", "Two Sum", "Given an array of integers...",
                Problem.Difficulty.EASY, "2 <= nums.length <= 10^4", "int[] nums, int target", "int[]");
        Pattern pattern = makePattern(1L, "two-pointers");
        Category category = makeCategory(1L, "Array");

        when(problemRepository.existsBySlug("two-sum")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);
        when(categoryRepository.findByName("Array")).thenReturn(Optional.of(category));
        when(patternRepository.findBySlug("two-pointers")).thenReturn(Optional.of(pattern));
        when(patternRepository.findPatternsByProblemIds(List.of(1L)))
                .thenReturn(List.<Object[]>of(new Object[]{1L, 1L, "Two Pointers", "two-pointers", "Use two pointers"}));

        ProblemExample savedEx1 = new ProblemExample();
        savedEx1.setExampleId(1L);
        savedEx1.setInputData("[2,7,11,15]");
        savedEx1.setExpectedOutput("[0,1]");
        savedEx1.setExplanation("nums[0]+nums[1] = 9");
        savedEx1.setDisplayOrder(1);

        ProblemExample savedEx2 = new ProblemExample();
        savedEx2.setExampleId(2L);
        savedEx2.setInputData("[3,2,4]");
        savedEx2.setExpectedOutput("[1,2]");
        savedEx2.setExplanation("nums[1]+nums[2] = 6");
        savedEx2.setDisplayOrder(2);

        when(problemExampleRepository.save(any(ProblemExample.class)))
                .thenReturn(savedEx1)
                .thenReturn(savedEx2);

        ProblemRequest request = new ProblemRequest(
                "two-sum",
                "Two Sum",
                "Given an array of integers...",
                Problem.Difficulty.EASY,
                "2 <= nums.length <= 10^4",
                "int[] nums, int target",
                "int[]",
                List.of(
                        new ExampleRequest("[2,7,11,15]", "[0,1]", "nums[0]+nums[1] = 9", 1),
                        new ExampleRequest("[3,2,4]", "[1,2]", "nums[1]+nums[2] = 6", 2)
                ),
                Set.of("Array"),
                Set.of("two-pointers"),
                List.of(
                        new TestCaseRequest("[1,2,3]", "[0,2]", false, false, 1),
                        new TestCaseRequest("[5,5]", "[0,1]", true, false, 2)
                ),
                null
        );

        // Act
        ProblemResponse response = problemServiceImpl.createProblem(request);

        // Assert
        assertNotNull(response);
        assertEquals("two-sum", response.slug());
        assertEquals("Two Sum", response.title());
        assertEquals(Problem.Difficulty.EASY, response.difficulty());
        assertEquals(Problem.ProblemStatus.DRAFT, response.status());
        assertEquals(2, response.examples().size());
        assertEquals("[2,7,11,15]", response.examples().get(0).inputData());
        assertEquals("[3,2,4]", response.examples().get(1).inputData());
        assertEquals(List.of("Array"), response.categories());
        assertEquals(1, response.patterns().size());
        assertEquals("two-pointers", response.patterns().get(0).slug());

        // Verify test cases were saved
        verify(problemTestCaseRepository, times(2)).save(any(ProblemTestCase.class));
        ArgumentCaptor<ProblemTestCase> tcCaptor = ArgumentCaptor.forClass(ProblemTestCase.class);
        verify(problemTestCaseRepository, times(2)).save(tcCaptor.capture());
        List<ProblemTestCase> savedTestCases = tcCaptor.getAllValues();
        assertEquals("[1,2,3]", savedTestCases.get(0).getInputData());
        assertEquals("[5,5]", savedTestCases.get(1).getInputData());
        assertFalse(savedTestCases.get(0).getIsPublic());
        assertTrue(savedTestCases.get(1).getIsPublic());
        assertFalse(savedTestCases.get(0).getIsHidden());
        assertFalse(savedTestCases.get(1).getIsHidden());
        assertNotNull(savedTestCases.get(0).getCreatedAt());
    }

    @Test
    void createProblem_withNullExamplesAndTestCases_shouldCreateProblemWithoutThem() {
        // Arrange
        Problem savedProblem = makeProblem(1L, "simple-problem", "Simple Problem", "A simple problem statement",
                Problem.Difficulty.MEDIUM, "n >= 1", "int n", "int");

        when(problemRepository.existsBySlug("simple-problem")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);
        when(patternRepository.findPatternsByProblemIds(List.of(1L)))
                .thenReturn(List.of());

        ProblemRequest request = new ProblemRequest(
                "simple-problem",
                "Simple Problem",
                "A simple problem statement",
                Problem.Difficulty.MEDIUM,
                "n >= 1",
                "int n",
                "int",
                null,
                Set.of(),
                Set.of(),
                null,
                null
        );

        // Act
        ProblemResponse response = problemServiceImpl.createProblem(request);

        // Assert
        assertNotNull(response);
        assertEquals("Simple Problem", response.title());
        assertEquals(Problem.Difficulty.MEDIUM, response.difficulty());
        assertTrue(response.examples().isEmpty());
        assertTrue(response.categories().isEmpty());
        assertTrue(response.patterns().isEmpty());
        assertTrue(response.languageConfigs().isEmpty());

        verify(problemExampleRepository, never()).save(any());
        verify(problemTestCaseRepository, never()).save(any());
        verify(problemLanguageConfigRepository, never()).save(any());
    }

    @Test
    void createProblem_withNewCategory_shouldAutoCreateCategory() {
        // Arrange
        Problem savedProblem = makeProblem(1L, "new-cat-problem", "New Category Problem", "Statement",
                Problem.Difficulty.EASY, "n >= 1", "int n", "void");
        Category newCategory = makeCategory(2L, "New Category");

        when(problemRepository.existsBySlug("new-cat-problem")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);
        when(categoryRepository.findByName("New Category")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
        when(patternRepository.findPatternsByProblemIds(List.of(1L)))
                .thenReturn(List.of());

        ProblemRequest request = new ProblemRequest(
                "new-cat-problem",
                "New Category Problem",
                "Statement",
                Problem.Difficulty.EASY,
                "n >= 1",
                "int n",
                "void",
                List.of(),
                Set.of("New Category"),
                Set.of(),
                List.of(),
                null
        );

        // Act
        ProblemResponse response = problemServiceImpl.createProblem(request);

        // Assert
        assertNotNull(response);
        assertEquals(List.of("New Category"), response.categories());
        verify(categoryRepository).save(argThat(cat -> "New Category".equals(cat.getName())));
    }

    @Test
    void createProblem_withExistingCategory_shouldReuseCategory() {
        // Arrange
        Problem savedProblem = makeProblem(1L, "existing-cat-problem", "Existing Category Problem", "Statement",
                Problem.Difficulty.EASY, "n >= 1", "int n", "void");
        Category existingCategory = makeCategory(1L, "Array");

        when(problemRepository.existsBySlug("existing-cat-problem")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);
        when(categoryRepository.findByName("Array")).thenReturn(Optional.of(existingCategory));
        when(patternRepository.findPatternsByProblemIds(List.of(1L)))
                .thenReturn(List.of());

        ProblemRequest request = new ProblemRequest(
                "existing-cat-problem",
                "Existing Category Problem",
                "Statement",
                Problem.Difficulty.EASY,
                "n >= 1",
                "int n",
                "void",
                List.of(),
                Set.of("Array"),
                Set.of(),
                List.of(),
                null
        );

        // Act
        ProblemResponse response = problemServiceImpl.createProblem(request);

        // Assert
        assertNotNull(response);
        assertEquals(List.of("Array"), response.categories());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createProblem_withMissingPattern_shouldThrowIllegalArgumentException() {
        // Arrange
        when(problemRepository.existsBySlug("missing-pattern-problem")).thenReturn(false);
        when(problemRepository.save(any(Problem.class)))
                .thenReturn(makeProblem(1L, "missing-pattern-problem", "Missing Pattern Problem", "Statement",
                        Problem.Difficulty.EASY, "n >= 1", "int n", "void"));
        when(patternRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

        ProblemRequest request = new ProblemRequest(
                "missing-pattern-problem",
                "Missing Pattern Problem",
                "Statement",
                Problem.Difficulty.EASY,
                "n >= 1",
                "int n",
                "void",
                List.of(),
                Set.of(),
                Set.of("nonexistent"),
                List.of(),
                null
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> problemServiceImpl.createProblem(request));
    }

    @Test
    void createProblem_withMultipleCategoriesAndPatterns_shouldAssignAll() {
        // Arrange
        Problem savedProblem = makeProblem(1L, "multi-cat-problem", "Multi Category Problem", "Statement",
                Problem.Difficulty.HARD, "n >= 1", "int n", "void");
        Category cat1 = makeCategory(1L, "Array");
        Category cat2 = makeCategory(2L, "Hash Table");
        Pattern pat1 = makePattern(1L, "two-pointers");
        Pattern pat2 = makePattern(2L, "hash-map");

        when(problemRepository.existsBySlug("multi-cat-problem")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);
        when(categoryRepository.findByName("Array")).thenReturn(Optional.of(cat1));
        when(categoryRepository.findByName("Hash Table")).thenReturn(Optional.of(cat2));
        when(patternRepository.findBySlug("two-pointers")).thenReturn(Optional.of(pat1));
        when(patternRepository.findBySlug("hash-map")).thenReturn(Optional.of(pat2));
        when(patternRepository.findPatternsByProblemIds(List.of(1L)))
                .thenReturn(List.<Object[]>of(
                        new Object[]{1L, 1L, "Two Pointers", "two-pointers", "Use two pointers"},
                        new Object[]{1L, 2L, "Hash Map", "hash-map", "Use hash map"}
                ));

        ProblemRequest request = new ProblemRequest(
                "multi-cat-problem",
                "Multi Category Problem",
                "Statement",
                Problem.Difficulty.HARD,
                "n >= 1",
                "int n",
                "void",
                List.of(),
                Set.of("Array", "Hash Table"),
                Set.of("two-pointers", "hash-map"),
                List.of(),
                null
        );

        // Act
        ProblemResponse response = problemServiceImpl.createProblem(request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.categories().size());
        assertEquals(List.of("Array", "Hash Table"), response.categories());
        assertEquals(2, response.patterns().size());
    }

    @Test
    void createProblem_shouldSetDraftStatusAndTimestamps() {
        // Arrange
        Problem savedProblem = makeProblem(1L, "timestamp-problem", "Timestamp Problem", "Statement",
                Problem.Difficulty.EASY, "n >= 1", "int n", "void");

        when(problemRepository.existsBySlug("timestamp-problem")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);
        when(patternRepository.findPatternsByProblemIds(List.of(1L)))
                .thenReturn(List.of());

        ProblemRequest request = new ProblemRequest(
                "timestamp-problem",
                "Timestamp Problem",
                "Statement",
                Problem.Difficulty.EASY,
                "n >= 1",
                "int n",
                "void",
                List.of(),
                Set.of(),
                Set.of(),
                List.of(),
                null
        );

        // Act
        ProblemResponse response = problemServiceImpl.createProblem(request);

        // Assert
        assertEquals(Problem.ProblemStatus.DRAFT, response.status());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());

        ArgumentCaptor<Problem> problemCaptor = ArgumentCaptor.forClass(Problem.class);
        verify(problemRepository, atLeastOnce()).save(problemCaptor.capture());
        Problem capturedProblem = problemCaptor.getAllValues().get(0);
        assertEquals(Problem.ProblemStatus.DRAFT, capturedProblem.getStatus());
        assertNotNull(capturedProblem.getCreatedAt());
        assertNotNull(capturedProblem.getUpdatedAt());
    }

    @Test
    void createProblem_withEmptyExamplesList_shouldNotSaveAnyExamples() {
        // Arrange
        Problem savedProblem = makeProblem(1L, "empty-examples-problem", "Empty Examples Problem", "Statement",
                Problem.Difficulty.EASY, "n >= 1", "int n", "void");

        when(problemRepository.existsBySlug("empty-examples-problem")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);
        when(patternRepository.findPatternsByProblemIds(List.of(1L)))
                .thenReturn(List.of());

        ProblemRequest request = new ProblemRequest(
                "empty-examples-problem",
                "Empty Examples Problem",
                "Statement",
                Problem.Difficulty.EASY,
                "n >= 1",
                "int n",
                "void",
                List.of(),
                Set.of(),
                Set.of(),
                List.of(),
                null
        );

        // Act
        ProblemResponse response = problemServiceImpl.createProblem(request);

        // Assert
        assertTrue(response.examples().isEmpty());
        verify(problemExampleRepository, never()).save(any());
    }

    @Test
    void createProblem_withEmptyTestCasesList_shouldNotSaveAnyTestCases() {
        // Arrange
        Problem savedProblem = makeProblem(1L, "empty-testcases-problem", "Empty TestCases Problem", "Statement",
                Problem.Difficulty.EASY, "n >= 1", "int n", "void");

        when(problemRepository.existsBySlug("empty-testcases-problem")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);
        when(patternRepository.findPatternsByProblemIds(List.of(1L)))
                .thenReturn(List.of());

        ProblemRequest request = new ProblemRequest(
                "empty-testcases-problem",
                "Empty TestCases Problem",
                "Statement",
                Problem.Difficulty.EASY,
                "n >= 1",
                "int n",
                "void",
                List.of(),
                Set.of(),
                Set.of(),
                List.of(),
                null
        );

        // Act
        ProblemResponse response = problemServiceImpl.createProblem(request);

        // Assert
        assertNotNull(response);
        verify(problemTestCaseRepository, never()).save(any());
    }

    @Test
    void createProblem_withLanguageConfigs_shouldSaveAndReturnLanguageConfigs() {
        // Arrange
        Problem savedProblem = makeProblem(1L, "lang-config-problem", "Language Config Problem", "Statement",
                Problem.Difficulty.EASY, "n >= 1", "int n", "int");

        when(problemRepository.existsBySlug("lang-config-problem")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);
        when(patternRepository.findPatternsByProblemIds(List.of(1L)))
                .thenReturn(List.of());

        ProblemLanguageConfig savedConfig = new ProblemLanguageConfig();
        savedConfig.setConfigId(1L);
        savedConfig.setLanguage("java");
        savedConfig.setClassName("Solution");
        savedConfig.setMethodName("twoSum");
        savedConfig.setReturnType("int[]");
        savedConfig.setParameterTypes("[\"int[]\", \"int\"]");
        savedConfig.setParameterNames("[\"nums\", \"target\"]");
        savedConfig.setStarterCode("class Solution {\n    public int[] twoSum(int[] nums, int target) {}\n}");

        when(problemLanguageConfigRepository.save(any(ProblemLanguageConfig.class))).thenReturn(savedConfig);

        ProblemRequest request = new ProblemRequest(
                "lang-config-problem",
                "Language Config Problem",
                "Statement",
                Problem.Difficulty.EASY,
                "n >= 1",
                "int n",
                "int",
                List.of(),
                Set.of(),
                Set.of(),
                List.of(),
                List.of(
                        new LanguageConfigRequest("java", "Solution", "twoSum", "int[]",
                                "[\"int[]\", \"int\"]", "[\"nums\", \"target\"]",
                                "class Solution {\n    public int[] twoSum(int[] nums, int target) {}\n}")
                )
        );

        // Act
        ProblemResponse response = problemServiceImpl.createProblem(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.languageConfigs().size());
        LanguageConfigResponse lc = response.languageConfigs().get(0);
        assertEquals("java", lc.language());
        assertEquals("Solution", lc.className());
        assertEquals("twoSum", lc.methodName());
        assertEquals("int[]", lc.returnType());
        verify(problemLanguageConfigRepository).save(any(ProblemLanguageConfig.class));
    }

    @Test
    void createProblem_withDuplicateSlug_shouldThrowIllegalArgumentException() {
        // Arrange
        when(problemRepository.existsBySlug("duplicate-slug")).thenReturn(true);

        ProblemRequest request = new ProblemRequest(
                "duplicate-slug",
                "Duplicate Slug Problem",
                "Statement",
                Problem.Difficulty.EASY,
                "n >= 1",
                "int n",
                "void",
                List.of(),
                Set.of(),
                Set.of(),
                List.of(),
                null
        );

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> problemServiceImpl.createProblem(request));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(problemRepository, never()).save(any());
    }

    @Test
    void createProblem_withHiddenTestCases_shouldSetIsHiddenTrue() {
        // Arrange
        Problem savedProblem = makeProblem(1L, "hidden-tc-problem", "Hidden Test Case Problem", "Statement",
                Problem.Difficulty.EASY, "n >= 1", "int n", "int");

        when(problemRepository.existsBySlug("hidden-tc-problem")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);
        when(patternRepository.findPatternsByProblemIds(List.of(1L)))
                .thenReturn(List.of());

        ProblemRequest request = new ProblemRequest(
                "hidden-tc-problem",
                "Hidden Test Case Problem",
                "Statement",
                Problem.Difficulty.EASY,
                "n >= 1",
                "int n",
                "int",
                List.of(),
                Set.of(),
                Set.of(),
                List.of(
                        new TestCaseRequest("[1,2,3]", "6", false, true, 1),
                        new TestCaseRequest("[4,5,6]", "15", false, false, 2)
                ),
                null
        );

        // Act
        problemServiceImpl.createProblem(request);

        // Assert
        ArgumentCaptor<ProblemTestCase> tcCaptor = ArgumentCaptor.forClass(ProblemTestCase.class);
        verify(problemTestCaseRepository, times(2)).save(tcCaptor.capture());
        List<ProblemTestCase> savedTestCases = tcCaptor.getAllValues();
        assertTrue(savedTestCases.get(0).getIsHidden());
        assertFalse(savedTestCases.get(1).getIsHidden());
        assertTrue(savedTestCases.get(0).getIsActive());
        assertNotNull(savedTestCases.get(0).getCreatedAt());
        assertNotNull(savedTestCases.get(0).getUpdatedAt());
    }
}
