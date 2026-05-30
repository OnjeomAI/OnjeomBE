package com.onjeom.backend.domain.learning.entity;

import com.onjeom.backend.domain.learning.enums.HighlightColor;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "highlights")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
public class Highlight extends BaseTimeEntity {

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
    private Integer startOffset;

    @Column(nullable = false)
    private Integer endOffset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private HighlightColor color;

    public void updateColor(HighlightColor color) {
        this.color = color;
    }
}
