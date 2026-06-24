package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.CampaignRulePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampaignRulePaymentRepository extends JpaRepository<CampaignRulePayment, Long> {
    List<CampaignRulePayment> findByMaChienDich(String maChienDich);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    void deleteByMaChienDich(String maChienDich);
}
