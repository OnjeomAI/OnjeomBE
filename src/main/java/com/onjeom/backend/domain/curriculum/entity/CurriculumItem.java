package com.onjeom.backend.domain.curriculum.entity;

import com.onjeom.backend.domain.curriculum.enums.CurriculumItemStatus;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "curriculum_items")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
public class CurriculumItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id", nullable = false)
    private Curriculum curriculum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private Integer stage;

    @Column(nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CurriculumItemStatus status;

    @Column
    private LocalDateTime scheduledAt;

    @Column
    private LocalDateTime completedAt;

    public void start() {
        this.status = CurriculumItemStatus.IN_PROGRESS;
    }

    public void complete() {
        this.status = CurriculumItemStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void skip() {
        this.status = CurriculumItemStatus.SKIPPED;
    }
}
