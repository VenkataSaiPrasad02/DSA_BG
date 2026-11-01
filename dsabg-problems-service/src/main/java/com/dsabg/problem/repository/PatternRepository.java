package com.dsabg.problem.repository;

import com.dsabg.problem.entity.Pattern;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PatternRepository extends JpaRepository<Pattern, Long> {

    Optional<Pattern> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = "categories")
    List<Pattern> findDistinctByCategories_CategoryIdIn(
            Collection<Long> categoryIds
    );

    @Query("""
        SELECT problem.problemId,
               pattern.patternId,
               pattern.name,
               pattern.slug,
               pattern.description
        FROM Pattern pattern
        JOIN pattern.problems problem
        WHERE problem.problemId IN :problemIds
        ORDER BY problem.problemId, pattern.name
        """)
    List<Object[]> findPatternsByProblemIds(
            @Param("problemIds") List<Long> problemIds
    );
}