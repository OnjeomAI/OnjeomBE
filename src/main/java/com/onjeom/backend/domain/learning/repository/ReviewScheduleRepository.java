package com.onjeom.backend.domain.learning.repository;

import com.onjeom.backend.domain.learning.entity.ReviewSchedule;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewScheduleRepository extends JpaRepository<ReviewSchedule, Long> {

    List<ReviewSchedule> findByUserAndNextReviewAtBefore(User user, LocalDateTime now);

    Optional<ReviewSchedule> findByUserAndProblem(User user, Problem problem);

    List<ReviewSchedule> findByUserOrderByNextReviewAtAsc(User user);
}
