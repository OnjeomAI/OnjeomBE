package com.onjeom.backend.domain.response.repository;

import com.onjeom.backend.domain.response.entity.Response;
import com.onjeom.backend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ResponseRepository extends JpaRepository<Response, Long> {

    List<Response> findByUserIdAndProblemIdOrderByCreatedAtAsc(Long userId, Long problemId);

    int countByUserIdAndProblemId(Long userId, Long problemId);

    boolean existsByIdAndUserId(Long id, Long userId);

    List<Response> findByUserAndCreatedAtAfter(User user, LocalDateTime dateTime);

    long countByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT AVG(r.finalScore) FROM Response r")
    Double findAverageFinalScore();

    List<Response> findByUserOrderByCreatedAtDesc(User user);

    Page<Response> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT r.user.id) FROM Response r WHERE r.createdAt > :dateTime")
    long countDistinctUsersByCreatedAtAfter(@Param("dateTime") LocalDateTime dateTime);
}
