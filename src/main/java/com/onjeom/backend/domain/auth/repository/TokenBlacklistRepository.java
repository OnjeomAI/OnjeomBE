package com.onjeom.backend.domain.auth.repository;

import com.onjeom.backend.domain.auth.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByTokenHash(String tokenHash);
}
