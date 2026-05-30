package com.onjeom.backend.domain.learning.entity;

import com.onjeom.backend.domain.learning.enums.CompetencyType;
import com.onjeom.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "knowledge_tracing")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
public class KnowledgeTracing {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompetencyType competencyType;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal pKnow;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal pLearn;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal pSlip;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal pGuess;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(boolean isCorrect) {
        double pL = pKnow.doubleValue();
        double pS = pSlip.doubleValue();
        double pG = pGuess.doubleValue();
        double pT = pLearn.doubleValue();

        double pCorrect = pL * (1 - pS) + (1 - pL) * pG;
        double pWrong   = pL * pS + (1 - pL) * (1 - pG);

        double pLNew = isCorrect
                ? (pL * (1 - pS)) / pCorrect
                : (pL * pS) / pWrong;

        pLNew = pLNew + (1 - pLNew) * pT;

        this.pKnow = BigDecimal.valueOf(Math.min(pLNew, 0.9999));
        this.updatedAt = LocalDateTime.now();
    }
}
