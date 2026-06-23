package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.StorePosKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StorePosKeyRepository extends JpaRepository<StorePosKey, Long> {
    Optional<StorePosKey> findByApiKeyHashAndTrangThai(String apiKeyHash, Integer trangThai);
}
