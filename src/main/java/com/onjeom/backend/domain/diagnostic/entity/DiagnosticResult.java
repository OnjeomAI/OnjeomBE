package com.onjeom.backend.domain.diagnostic.entity;

import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "diagnostic_results")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
public class DiagnosticResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 5, scale = 3)
    private BigDecimal theta;

    @Column(nullable = false)
    private Integer factualScore;

    @Column(nullable = false)
    private Integer inferentialScore;

    @Column(nullable = false)
    private Integer criticalScore;

    @Column(nullable = false)
    private Integer vocabularyScore;

    @Column(nullable = false)
    private Integer logicalScore;

    @Column(nullable = false)
    private Integer level;
}
