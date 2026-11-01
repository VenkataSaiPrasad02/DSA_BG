package com.dsabg.problem.repository;

import com.dsabg.problem.entity.Problem;
import com.dsabg.problem.entity.ProblemExample;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long>,
        JpaSpecificationExecutor<Problem> {

    Optional<Problem> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Problem> findByStatusOrderByProblemIdAsc(
            Problem.ProblemStatus status,
            Pageable pageable
    );


    @Query("""
            SELECT p.problemId, c.name
            FROM Problem p
            JOIN p.categories c
            WHERE p.problemId IN :problemIds
            """)
    List<Object[]> findCategoriesByProblemId(
            @Param("problemIds") List<Long> problemIds
    );



    @Query("""
    SELECT DISTINCT p
    FROM Problem p
    LEFT JOIN FETCH p.patterns
    WHERE p.problemId = :problemId
""")
    Optional<Problem> findByIdWithPatterns(
            @Param("problemId") Long problemId
    );
    @Query("""
    SELECT DISTINCT p.problemId
    FROM Problem p
    JOIN p.categories c
    WHERE c.categoryId IN :categoryIds
      AND p.difficulty = :difficulty
      AND p.status = com.dsabg.problem.entity.Problem.ProblemStatus.PUBLISHED
""")
    List<Long> findEligibleProblemIds(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("difficulty") Problem.Difficulty difficulty
    );
}
