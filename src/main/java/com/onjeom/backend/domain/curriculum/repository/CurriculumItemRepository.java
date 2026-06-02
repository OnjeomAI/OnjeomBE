package com.onjeom.backend.domain.curriculum.repository;

import com.onjeom.backend.domain.curriculum.entity.Curriculum;
import com.onjeom.backend.domain.curriculum.entity.CurriculumItem;
import com.onjeom.backend.domain.curriculum.enums.CurriculumItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CurriculumItemRepository extends JpaRepository<CurriculumItem, Long> {

    void deleteByProblemId(Long problemId);

    List<CurriculumItem> findByCurriculumOrderByStageAscOrderIndexAsc(Curriculum curriculum);

    List<CurriculumItem> findByCurriculumAndStage(Curriculum curriculum, Integer stage);

    List<CurriculumItem> findByCurriculumAndStatus(Curriculum curriculum, CurriculumItemStatus status);

    int countByCurriculumAndStatus(Curriculum curriculum, CurriculumItemStatus status);

    Optional<CurriculumItem> findTopByCurriculumAndStatusOrderByOrderIndexAsc(
            Curriculum curriculum, CurriculumItemStatus status);
}
