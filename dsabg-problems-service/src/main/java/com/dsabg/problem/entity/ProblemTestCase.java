package com.dsabg.problem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "problem_test_cases",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_problem_testcase_order",
                        columnNames = {"problem_id", "display_order"}
                )
        }
)
@Getter
@Setter
public class ProblemTestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_case_id")
    private Long testCaseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "input_data", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String inputData;

    @Column(name = "expected_output", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String expectedOutput;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = true;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
