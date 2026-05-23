package com.onjeom.backend.domain.problem.repository;

import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.enums.ReadingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findByReadingType(ReadingType readingType);

    List<Problem> findByDifficulty(Integer difficulty);

    List<Problem> findByReadingTypeAndDifficulty(ReadingType readingType, Integer difficulty);

    Page<Problem> findAll(Pageable pageable);
}
