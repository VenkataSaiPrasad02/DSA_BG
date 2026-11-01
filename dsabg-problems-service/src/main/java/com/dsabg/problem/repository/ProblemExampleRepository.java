package com.dsabg.problem.repository;

import com.dsabg.problem.entity.ProblemExample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemExampleRepository
        extends JpaRepository<ProblemExample, Long> {

    List<ProblemExample> findByProblemProblemIdOrderByDisplayOrderAsc(
            Long problemId
    );
    @Query("""
            SELECT e
            FROM ProblemExample e
            WHERE e.problem.problemId IN :problemIds
            ORDER BY e.problem.problemId ASC, e.displayOrder ASC
            """)
    List<ProblemExample> findExamplesByProblemIds(
            @Param("problemIds") List<Long> problemIds
    );
    List<ProblemExample> findByProblemProblemIdIn(List<Long> problemIds);
}