package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.CampaignRuleSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampaignRuleSkuRepository extends JpaRepository<CampaignRuleSku, Long> {
    List<CampaignRuleSku> findByIdChienDich(Long idChienDich);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    void deleteByIdChienDich(Long idChienDich);
}
