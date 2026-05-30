package com.onjeom.backend.domain.learning.repository;

import com.onjeom.backend.domain.learning.entity.KnowledgeTracing;
import com.onjeom.backend.domain.learning.enums.CompetencyType;
import com.onjeom.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeTracingRepository extends JpaRepository<KnowledgeTracing, Long> {

    Optional<KnowledgeTracing> findByUserAndCompetencyType(User user, CompetencyType competencyType);

    List<KnowledgeTracing> findByUser(User user);
}
