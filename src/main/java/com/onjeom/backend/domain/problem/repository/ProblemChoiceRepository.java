package com.onjeom.backend.domain.problem.repository;

import com.onjeom.backend.domain.problem.entity.ProblemChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemChoiceRepository extends JpaRepository<ProblemChoice, Long> {

    List<ProblemChoice> findByProblemId(Long problemId);

    void deleteAllByProblemId(Long problemId);
}
