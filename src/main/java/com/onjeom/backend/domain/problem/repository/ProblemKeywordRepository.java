package com.onjeom.backend.domain.problem.repository;

import com.onjeom.backend.domain.problem.entity.ProblemKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemKeywordRepository extends JpaRepository<ProblemKeyword, Long> {

    List<ProblemKeyword> findByProblemId(Long problemId);

    void deleteAllByProblemId(Long problemId);

    int countByProblemId(Long problemId);
}
