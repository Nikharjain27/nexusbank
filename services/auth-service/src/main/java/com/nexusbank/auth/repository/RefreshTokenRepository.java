package com.nexusbank.auth.repository;

import com.nexusbank.auth.domain.entity.RefreshToken;
import com.nexusbank.auth.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUser(User user);
}