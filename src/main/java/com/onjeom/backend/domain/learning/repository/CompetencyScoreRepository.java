package com.onjeom.backend.domain.learning.repository;

import com.onjeom.backend.domain.learning.entity.CompetencyScore;
import com.onjeom.backend.domain.learning.enums.CompetencyType;
import com.onjeom.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompetencyScoreRepository extends JpaRepository<CompetencyScore, Long> {

    List<CompetencyScore> findByUserOrderByMeasuredAtDesc(User user);

    Optional<CompetencyScore> findTopByUserAndCompetencyTypeOrderByMeasuredAtDesc(
            User user, CompetencyType competencyType);

    List<CompetencyScore> findByUserAndCompetencyTypeOrderByMeasuredAtDesc(
            User user, CompetencyType competencyType);
}
