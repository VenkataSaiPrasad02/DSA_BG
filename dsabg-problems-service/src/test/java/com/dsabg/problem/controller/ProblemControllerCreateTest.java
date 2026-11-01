package com.dsabg.problem.controller;

import com.dsabg.problem.dto.request.ExampleRequest;
import com.dsabg.problem.dto.request.LanguageConfigRequest;
import com.dsabg.problem.dto.request.ProblemRequest;
import com.dsabg.problem.dto.request.TestCaseRequest;
import com.dsabg.problem.dto.response.LanguageConfigResponse;
import com.dsabg.problem.dto.response.PatternResponse;
import com.dsabg.problem.dto.response.ProblemExampleResponse;
import com.dsabg.problem.dto.response.ProblemResponse;
import com.dsabg.problem.entity.Problem;
import com.dsabg.problem.repository.ProblemRepository;
import com.dsabg.problem.service.ProblemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProblemController.class)
class ProblemControllerCreateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProblemService problemService;

    @MockitoBean
    private ProblemRepository problemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProblem_withValidRequest_shouldReturn201AndProblemResponse() throws Exception {
        // Arrange
        ProblemResponse expectedResponse = new ProblemResponse(
                1L,
                "two-sum",
                "Two Sum",
                "Given an array of integers...",
                Problem.Difficulty.EASY,
                "2 <= nums.length <= 10^4",
                "int[] nums, int target",
                "int[]",
                Problem.ProblemStatus.DRAFT,
                LocalDateTime.of(2026, 10, 31, 7, 0, 0),
                LocalDateTime.of(2026, 10, 31, 7, 0, 0),
                List.of(
                        new ProblemExampleResponse(1L, "[2,7,11,15]", "[0,1]", "nums[0]+nums[1] = 9", 1)
                ),
                List.of("Array"),
                List.of(new PatternResponse(1L, "Two Pointers", "two-pointers", "Use two pointers")),
                List.of()
        );

        when(problemService.createProblem(any(ProblemRequest.class))).thenReturn(expectedResponse);

        ProblemRequest request = new ProblemRequest(
                "two-sum",
                "Two Sum",
                "Given an array of integers...",
                Problem.Difficulty.EASY,
                "2 <= nums.length <= 10^4",
                "int[] nums, int target",
                "int[]",
                List.of(
                        new ExampleRequest("[2,7,11,15]", "[0,1]", "nums[0]+nums[1] = 9", 1)
                ),
                Set.of("Array"),
                Set.of("two-pointers"),
                List.of(
                        new TestCaseRequest("[1,2,3]", "[0,2]", false, true, 1)
                ),
                null
        );

        // Act & Assert
        mockMvc.perform(post("/problem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("two-sum"))
                .andExpect(jsonPath("$.title").value("Two Sum"))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.examples.length()").value(1))
                .andExpect(jsonPath("$.examples[0].inputData").value("[2,7,11,15]"))
                .andExpect(jsonPath("$.categories.length()").value(1))
                .andExpect(jsonPath("$.categories[0]").value("Array"))
                .andExpect(jsonPath("$.patterns.length()").value(1))
                .andExpect(jsonPath("$.patterns[0].slug").value("two-pointers"));

        verify(problemService).createProblem(any(ProblemRequest.class));
    }

    @Test
    void createProblem_withMinimalRequest_shouldReturn201() throws Exception {
        // Arrange
        ProblemResponse expectedResponse = new ProblemResponse(
                2L,
                "simple-problem",
                "Simple Problem",
                "A simple problem",
                Problem.Difficulty.MEDIUM,
                "n >= 1",
                "int n",
                "int",
                Problem.ProblemStatus.DRAFT,
                LocalDateTime.of(2026, 10, 31, 8, 0, 0),
                LocalDateTime.of(2026, 10, 31, 8, 0, 0),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        when(problemService.createProblem(any(ProblemRequest.class))).thenReturn(expectedResponse);

        ProblemRequest request = new ProblemRequest(
                "simple-problem",
                "Simple Problem",
                "A simple problem",
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

        // Act & Assert
        mockMvc.perform(post("/problem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("simple-problem"))
                .andExpect(jsonPath("$.examples").isEmpty())
                .andExpect(jsonPath("$.categories").isEmpty())
                .andExpect(jsonPath("$.patterns").isEmpty())
                .andExpect(jsonPath("$.languageConfigs").isEmpty());
    }

    @Test
    void createProblem_withHardDifficulty_shouldReturnCorrectDifficulty() throws Exception {
        // Arrange
        ProblemResponse expectedResponse = new ProblemResponse(
                3L,
                "hard-problem",
                "Hard Problem",
                "A hard problem",
                Problem.Difficulty.HARD,
                "n >= 1",
                "int n",
                "void",
                Problem.ProblemStatus.DRAFT,
                LocalDateTime.of(2026, 10, 31, 9, 0, 0),
                LocalDateTime.of(2026, 10, 31, 9, 0, 0),
                List.of(),
                List.of("Dynamic Programming", "Graph"),
                List.of(),
                List.of()
        );

        when(problemService.createProblem(any(ProblemRequest.class))).thenReturn(expectedResponse);

        ProblemRequest request = new ProblemRequest(
                "hard-problem",
                "Hard Problem",
                "A hard problem",
                Problem.Difficulty.HARD,
                "n >= 1",
                "int n",
                "void",
                List.of(),
                Set.of("Dynamic Programming", "Graph"),
                Set.of(),
                List.of(),
                null
        );

        // Act & Assert
        mockMvc.perform(post("/problem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.difficulty").value("HARD"))
                .andExpect(jsonPath("$.categories.length()").value(2));
    }

    @Test
    void createProblem_withMultipleExamplesAndTestCases_shouldReturnAllExamples() throws Exception {
        // Arrange
        ProblemResponse expectedResponse = new ProblemResponse(
                4L,
                "multi-example-problem",
                "Multi Example Problem",
                "Statement",
                Problem.Difficulty.EASY,
                "n >= 1",
                "int n",
                "int",
                Problem.ProblemStatus.DRAFT,
                LocalDateTime.of(2026, 10, 31, 10, 0, 0),
                LocalDateTime.of(2026, 10, 31, 10, 0, 0),
                List.of(
                        new ProblemExampleResponse(1L, "[1,2,3]", "6", "Sum of all elements", 1),
                        new ProblemExampleResponse(2L, "[4,5,6]", "15", "Sum of all elements", 2),
                        new ProblemExampleResponse(3L, "[]", "0", "Empty array", 3)
                ),
                List.of("Array"),
                List.of(),
                List.of()
        );

        when(problemService.createProblem(any(ProblemRequest.class))).thenReturn(expectedResponse);

        ProblemRequest request = new ProblemRequest(
                "multi-example-problem",
                "Multi Example Problem",
                "Statement",
                Problem.Difficulty.EASY,
                "n >= 1",
                "int n",
                "int",
                List.of(
                        new ExampleRequest("[1,2,3]", "6", "Sum of all elements", 1),
                        new ExampleRequest("[4,5,6]", "15", "Sum of all elements", 2),
                        new ExampleRequest("[]", "0", "Empty array", 3)
                ),
                Set.of("Array"),
                Set.of(),
                List.of(
                        new TestCaseRequest("[100]", "100", false, true, 1),
                        new TestCaseRequest("[-1,-2]", "-3", false, true, 2)
                ),
                null
        );

        // Act & Assert
        mockMvc.perform(post("/problem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.examples.length()").value(3))
                .andExpect(jsonPath("$.examples[0].inputData").value("[1,2,3]"))
                .andExpect(jsonPath("$.examples[1].expectedOutput").value("15"))
                .andExpect(jsonPath("$.examples[2].explanation").value("Empty array"));
    }

    @Test
    void createProblem_withInvalidJson_shouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/problem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProblem_withMissingRequiredFields_shouldReturn400() throws Exception {
        // Arrange - JSON missing required "slug" field
        String invalidJson = """
                {
                    "title": "Missing Slug",
                    "statementMd": "Test",
                    "difficulty": "EASY",
                    "constraintsText": "n >= 1",
                    "inputFormat": "int n",
                    "outputFormat": "int"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/problem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProblem_withLanguageConfigs_shouldReturn201WithLanguageConfigs() throws Exception {
        // Arrange
        ProblemResponse expectedResponse = new ProblemResponse(
                5L,
                "lang-config-problem",
                "Language Config Problem",
                "Statement",
                Problem.Difficulty.EASY,
                "n >= 1",
                "int n",
                "int",
                Problem.ProblemStatus.DRAFT,
                LocalDateTime.of(2026, 10, 31, 11, 0, 0),
                LocalDateTime.of(2026, 10, 31, 11, 0, 0),
                List.of(),
                List.of(),
                List.of(),
                List.of(
                        new LanguageConfigResponse(1L, "java", "Solution", "twoSum", "int[]",
                                "[\"int[]\", \"int\"]", "[\"nums\", \"target\"]",
                                "class Solution {}")
                )
        );

        when(problemService.createProblem(any(ProblemRequest.class))).thenReturn(expectedResponse);

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
                                "[\"int[]\", \"int\"]", "[\"nums\", \"target\"]", "class Solution {}")
                )
        );

        // Act & Assert
        mockMvc.perform(post("/problem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.languageConfigs.length()").value(1))
                .andExpect(jsonPath("$.languageConfigs[0].language").value("java"))
                .andExpect(jsonPath("$.languageConfigs[0].className").value("Solution"))
                .andExpect(jsonPath("$.languageConfigs[0].methodName").value("twoSum"));
    }
}
