package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.CampaignRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CampaignRuleRepository extends JpaRepository<CampaignRule, Long> {
    Optional<CampaignRule> findByMaChienDich(String maChienDich);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    void deleteByMaChienDich(String maChienDich);
}
