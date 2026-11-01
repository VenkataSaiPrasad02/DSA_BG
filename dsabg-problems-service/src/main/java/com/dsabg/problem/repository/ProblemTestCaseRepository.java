package com.dsabg.problem.repository;

import com.dsabg.problem.entity.ProblemTestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemTestCaseRepository extends JpaRepository<ProblemTestCase, Long> {

    List<ProblemTestCase> findByProblemProblemIdOrderByDisplayOrderAsc(Long problemId);

    List<ProblemTestCase> findByProblemProblemIdInOrderByDisplayOrderAsc(List<Long> problemIds);

    void deleteByProblemProblemId(Long problemId);

    List<ProblemTestCase> findByProblem_ProblemIdAndIsActiveTrueOrderByDisplayOrderAsc(
            Long problemId
    );
}
