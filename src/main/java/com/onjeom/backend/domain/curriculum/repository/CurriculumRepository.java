package com.onjeom.backend.domain.curriculum.repository;

import com.onjeom.backend.domain.curriculum.entity.Curriculum;
import com.onjeom.backend.domain.curriculum.enums.CurriculumStatus;
import com.onjeom.backend.domain.diagnostic.entity.DiagnosticResult;
import com.onjeom.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

    Optional<Curriculum> findTopByUserAndStatusOrderByCreatedAtDesc(User user, CurriculumStatus status);

    List<Curriculum> findByUser(User user);

    Optional<Curriculum> findByDiagnostic(DiagnosticResult diagnostic);
}
