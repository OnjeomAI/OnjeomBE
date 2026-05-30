package com.onjeom.backend.domain.response.entity;

import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "responses")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Response {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "curriculum_item_id")
    private Long curriculumItemId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answerText;

    @Column(nullable = false)
    private Integer responseTimeSec;

    @Column(nullable = false)
    private Integer rawScore;

    @Column(nullable = false)
    private Integer finalScore;

    @Column(columnDefinition = "TEXT")
    private String feedbackText;

    @Column(length = 50)
    private String errorType;

    @Column(columnDefinition = "TEXT")
    private String scoringBasis;

    @Column(nullable = false)
    private Integer attemptNumber;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
