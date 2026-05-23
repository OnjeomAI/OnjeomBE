package com.onjeom.backend.domain.response.repository;

import com.onjeom.backend.domain.response.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResponseRepository extends JpaRepository<Response, Long> {

    List<Response> findByUserIdAndProblemIdOrderByCreatedAtAsc(Long userId, Long problemId);

    int countByUserIdAndProblemId(Long userId, Long problemId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
