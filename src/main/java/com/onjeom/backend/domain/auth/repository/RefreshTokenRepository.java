package com.onjeom.backend.domain.auth.repository;

import com.onjeom.backend.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUserId(Long userId);

    void deleteByTokenHash(String tokenHash);

    void deleteAllByUserId(Long userId);
}
