package com.onjeom.backend.domain.auth.entity;

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
@Table(name = "email_verifications")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
public class EmailVerification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 6)
    private String otpCode;

    @Column(nullable = false)
    private Boolean isUsed;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void use() {
        this.isUsed = true;
    }
}
