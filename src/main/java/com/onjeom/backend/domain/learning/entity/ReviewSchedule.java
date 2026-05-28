package com.onjeom.backend.domain.learning.entity;

import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "review_schedules")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
public class ReviewSchedule {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private Integer reviewCount;

    @Column(nullable = false)
    private LocalDateTime nextReviewAt;

    @Column
    private LocalDateTime lastReviewedAt;

    private static final int[] REVIEW_INTERVALS = {1, 3, 7, 14, 30};

    public void completeReview() {
        this.lastReviewedAt = LocalDateTime.now();
        this.reviewCount++;
        int idx = Math.min(reviewCount, REVIEW_INTERVALS.length - 1);
        this.nextReviewAt = LocalDateTime.now().plusDays(REVIEW_INTERVALS[idx]);
    }
}
