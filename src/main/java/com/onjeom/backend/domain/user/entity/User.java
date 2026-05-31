package com.onjeom.backend.domain.user.entity;

import com.onjeom.backend.domain.user.enums.FontSize;
import com.onjeom.backend.domain.user.enums.UserRole;
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
@Table(name = "users")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private Integer dailyGoal;

    @Column(nullable = false)
    private Boolean emailVerified;

    @Column(nullable = false)
    private Boolean alarmEnabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private FontSize fontSize = FontSize.MEDIUM;

    public void verifyEmail() {
        this.emailVerified = true;
        this.role = UserRole.STUDENT;
    }

    public void updateProfile(String nickname, Boolean alarmEnabled, Integer dailyGoal, FontSize fontSize) {
        this.nickname = nickname;
        this.alarmEnabled = alarmEnabled;
        this.dailyGoal = dailyGoal;
        if (fontSize != null) this.fontSize = fontSize;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateRole(UserRole role) {
        this.role = role;
    }
}
