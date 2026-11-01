package com.dsabg.problem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "problem_language_configs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_problem_language",
                        columnNames = {"problem_id", "language"}
                )
        }
)
@Getter
@Setter
public class ProblemLanguageConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, length = 50)
    private String language;

    @Column(name = "class_name", length = 200)
    private String className;

    @Column(name = "method_name", length = 200)
    private String methodName;

    @Column(name = "return_type", length = 200)
    private String returnType;

    @Column(name = "parameter_types", columnDefinition = "JSON")
    private String parameterTypes;

    @Column(name = "parameter_names", columnDefinition = "JSON")
    private String parameterNames;

    @Column(name = "starter_code", columnDefinition = "MEDIUMTEXT")
    private String starterCode;
}
