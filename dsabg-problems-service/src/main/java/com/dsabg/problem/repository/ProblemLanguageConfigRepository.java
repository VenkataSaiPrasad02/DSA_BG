package com.dsabg.problem.repository;

import com.dsabg.problem.entity.ProblemLanguageConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemLanguageConfigRepository extends JpaRepository<ProblemLanguageConfig, Long> {

    List<ProblemLanguageConfig> findByProblemProblemId(Long problemId);

    List<ProblemLanguageConfig> findByProblemProblemIdIn(List<Long> problemIds);

    Optional<ProblemLanguageConfig> findByProblemProblemIdAndLanguage(Long problemId, String language);

    void deleteByProblemProblemId(Long problemId);

    Optional<ProblemLanguageConfig>
    findByProblem_ProblemIdAndLanguageIgnoreCase(
            Long problemId,
            String language
    );

    List<ProblemLanguageConfig> findByProblem_ProblemId(Long problemId);
}
