package com.onjeom.backend.domain.learning.enums;

public enum CompetencyLevel {
    초급, 중급, 고급, 심화;

    public static CompetencyLevel from(double score) {
        if (score >= 80) return 심화;
        if (score >= 60) return 고급;
        if (score >= 40) return 중급;
        return 초급;
    }
}
