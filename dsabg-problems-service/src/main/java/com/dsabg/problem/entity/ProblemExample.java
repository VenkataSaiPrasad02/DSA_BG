
package com.dsabg.problem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "problem_examples",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_problem_example_order",
                        columnNames = {"problem_id", "display_order"}
                )
        }
)
@Getter
@Setter
public class ProblemExample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "example_id")
    private Long exampleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "input_data", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String inputData;

    @Column(name = "expected_output", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String expectedOutput;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 1;
}
