package com.onjeom.backend.domain.diagnostic.repository;

import com.onjeom.backend.domain.diagnostic.entity.DiagnosticResult;
import com.onjeom.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiagnosticResultRepository extends JpaRepository<DiagnosticResult, Long> {

    Optional<DiagnosticResult> findTopByUserOrderByCreatedAtDesc(User user);

    List<DiagnosticResult> findByUserOrderByCreatedAtDesc(User user);
}
