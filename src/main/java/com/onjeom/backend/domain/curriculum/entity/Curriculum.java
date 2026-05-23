package com.onjeom.backend.domain.curriculum.entity;

import com.onjeom.backend.domain.curriculum.enums.CurriculumStatus;
import com.onjeom.backend.domain.diagnostic.entity.DiagnosticResult;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "curricula")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
public class Curriculum extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_id", nullable = false)
    private DiagnosticResult diagnostic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CurriculumStatus status;

    @Column(nullable = false)
    private Integer currentStage;

    public void updateStage(Integer stage) {
        this.currentStage = stage;
    }

    public void pause() {
        this.status = CurriculumStatus.PAUSED;
    }

    public void resume() {
        this.status = CurriculumStatus.ACTIVE;
    }

    public void complete() {
        this.status = CurriculumStatus.COMPLETED;
    }
}
