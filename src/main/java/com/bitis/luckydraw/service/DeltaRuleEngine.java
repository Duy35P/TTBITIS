package com.bitis.luckydraw.service;

import com.bitis.luckydraw.dto.InvoiceRequestDTO;
import com.bitis.luckydraw.model.CampaignRule;
import com.bitis.luckydraw.model.CampaignRulePayment;
import com.bitis.luckydraw.model.CampaignRuleSku;
import com.bitis.luckydraw.repository.CampaignRulePaymentRepository;
import com.bitis.luckydraw.repository.CampaignRuleRepository;
import com.bitis.luckydraw.repository.CampaignRuleSkuRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeltaRuleEngine {

    private final CampaignRuleRepository ruleRepo;
    private final CampaignRulePaymentRepository rulePaymentRepo;
    private final CampaignRuleSkuRepository ruleSkuRepo;

    public DeltaRuleEngine(CampaignRuleRepository ruleRepo, 
                           CampaignRulePaymentRepository rulePaymentRepo, 
                           CampaignRuleSkuRepository ruleSkuRepo) {
        this.ruleRepo = ruleRepo;
        this.rulePaymentRepo = rulePaymentRepo;
        this.ruleSkuRepo = ruleSkuRepo;
    }

    public int calculateTurns(String maChienDich, Double deltaAmount, String paymentMethod, List<InvoiceRequestDTO.SkuItem> skuList) {
        if (deltaAmount <= 0) {
            return 0; // Khi Delta < 0 hoặc = 0 thì không làm gì cả
        }

        int totalTurns = 0;

        // 1. Tính lượt theo tổng tiền (min order value)
        Optional<CampaignRule> ruleOpt = ruleRepo.findByMaChienDich(maChienDich);
        if (ruleOpt.isPresent()) {
            CampaignRule rule = ruleOpt.get();
            if (rule.getGiaTriDonHangToiThieu() != null && rule.getGiaTriDonHangToiThieu() > 0) {
                totalTurns += (int) (deltaAmount / rule.getGiaTriDonHangToiThieu());
            }
        }

        // 2. Tính lượt ưu đãi theo Payment Method
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            List<CampaignRulePayment> paymentRules = rulePaymentRepo.findByMaChienDich(maChienDich);
            for (CampaignRulePayment pr : paymentRules) {
                if (pr.getPhuongThucThanhToan() != null && pr.getPhuongThucThanhToan().equalsIgnoreCase(paymentMethod)) {
                    totalTurns += pr.getSoLuotThuong() != null ? pr.getSoLuotThuong() : 0;
                }
            }
        }

        // 3. Tính lượt ưu đãi theo SKU
        if (skuList != null && !skuList.isEmpty()) {
            List<CampaignRuleSku> skuRules = ruleSkuRepo.findByMaChienDich(maChienDich);
            for (InvoiceRequestDTO.SkuItem item : skuList) {
                for (CampaignRuleSku sr : skuRules) {
                    if (sr.getMaSku() != null && sr.getMaSku().equalsIgnoreCase(item.getSku())) {
                        totalTurns += (sr.getSoLuotThuong() != null ? sr.getSoLuotThuong() : 0) * (item.getQuantity() != null ? item.getQuantity() : 1);
                    }
                }
            }
        }

        return totalTurns;
    }
}
