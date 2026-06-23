package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.GameAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameAccessTokenRepository extends JpaRepository<GameAccessToken, Long> {
    Optional<GameAccessToken> findByToken(String token);
}
