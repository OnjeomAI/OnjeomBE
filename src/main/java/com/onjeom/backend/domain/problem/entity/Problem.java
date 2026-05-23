package com.onjeom.backend.domain.problem.entity;

import com.onjeom.backend.domain.problem.dto.request.UpdateProblemRequest;
import com.onjeom.backend.domain.problem.enums.ProblemType;
import com.onjeom.backend.domain.problem.enums.ReadingType;
import com.onjeom.backend.domain.problem.enums.VectorIndexStatus;
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
@Table(name = "problems")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
public class Problem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String passageText;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProblemType problemType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReadingType readingType;

    @Column(nullable = false)
    private Integer difficulty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String modelAnswer;

    @Column(nullable = false)
    private Boolean vectorIndexed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VectorIndexStatus vectorIndexStatus;

    public void update(UpdateProblemRequest request) {
        if (request.passageText() != null) this.passageText = request.passageText();
        if (request.questionText() != null) this.questionText = request.questionText();
        if (request.readingType() != null) this.readingType = request.readingType();
        if (request.difficulty() != null) this.difficulty = request.difficulty();
        if (request.modelAnswer() != null) this.modelAnswer = request.modelAnswer();
    }

    public void updateVectorIndexStatus(VectorIndexStatus status) {
        this.vectorIndexStatus = status;
        if (status == VectorIndexStatus.DONE) {
            this.vectorIndexed = true;
        }
    }
}
