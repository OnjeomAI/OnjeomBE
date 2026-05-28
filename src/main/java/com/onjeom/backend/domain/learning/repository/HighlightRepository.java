package com.onjeom.backend.domain.learning.repository;

import com.onjeom.backend.domain.learning.entity.Highlight;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HighlightRepository extends JpaRepository<Highlight, Long> {

    List<Highlight> findByUserAndProblem(User user, Problem problem);

    Optional<Highlight> findByUserAndProblemAndStartOffsetAndEndOffset(
            User user, Problem problem, Integer startOffset, Integer endOffset);

    void deleteByUserAndProblemAndStartOffsetAndEndOffset(
            User user, Problem problem, Integer startOffset, Integer endOffset);
}
