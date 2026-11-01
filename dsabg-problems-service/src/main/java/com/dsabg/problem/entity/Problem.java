
package com.dsabg.problem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "problems")
@Getter
@Setter
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id")
    private Long problemId;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "statement_md", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String statementMd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @ManyToMany(mappedBy = "problems", fetch = FetchType.LAZY)
    private Set<Pattern> patterns = new HashSet<>();

    @Column(name = "constraints_text", nullable = false, columnDefinition = "TEXT")
    private String constraintsText;

    @Column(name = "input_format", nullable = false, columnDefinition = "TEXT")
    private String inputFormat;

    @Column(name = "output_format", nullable = false, columnDefinition = "TEXT")
    private String outputFormat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemStatus status = ProblemStatus.DRAFT;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "problem_categories",
            joinColumns = @JoinColumn(name = "problem_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();


    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public enum ProblemStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
}
