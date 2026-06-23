package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Campaign;
import com.bitis.luckydraw.repository.CampaignRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.model.CampaignStore;
import com.bitis.luckydraw.model.CampaignRule;
import com.bitis.luckydraw.model.CampaignRulePayment;
import com.bitis.luckydraw.model.CampaignRuleSku;
import com.bitis.luckydraw.repository.StoreRepository;
import com.bitis.luckydraw.repository.CampaignStoreRepository;
import com.bitis.luckydraw.repository.CampaignRuleRepository;
import com.bitis.luckydraw.repository.CampaignRulePaymentRepository;
import com.bitis.luckydraw.repository.CampaignRuleSkuRepository;
import com.bitis.luckydraw.dto.CampaignRuleForm;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/campaigns")
public class AdminCampaignController {

    private final CampaignRepository campaignRepository;
    private final StoreRepository storeRepository;
    private final CampaignStoreRepository campaignStoreRepository;
    private final CampaignRuleRepository campaignRuleRepository;
    private final CampaignRulePaymentRepository campaignRulePaymentRepository;
    private final CampaignRuleSkuRepository campaignRuleSkuRepository;

    public AdminCampaignController(CampaignRepository campaignRepository, StoreRepository storeRepository, CampaignStoreRepository campaignStoreRepository,
                                   CampaignRuleRepository campaignRuleRepository, CampaignRulePaymentRepository campaignRulePaymentRepository, CampaignRuleSkuRepository campaignRuleSkuRepository) {
        this.campaignRepository = campaignRepository;
        this.storeRepository = storeRepository;
        this.campaignStoreRepository = campaignStoreRepository;
        this.campaignRuleRepository = campaignRuleRepository;
        this.campaignRulePaymentRepository = campaignRulePaymentRepository;
        this.campaignRuleSkuRepository = campaignRuleSkuRepository;
    }

    @GetMapping
    public String listCampaigns(Model model) {
        model.addAttribute("campaigns", campaignRepository.findAll());
        return "admin/campaign-list";
    }

    @PostMapping("/save")
    public String saveCampaign(@ModelAttribute Campaign campaign, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            campaignRepository.save(campaign);
        } catch (Exception e) {
            String errorMsg = e.getCause() != null && e.getCause().getCause() != null 
                ? e.getCause().getCause().getMessage() 
                : e.getMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        }
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/toggle-status")
    public String toggleStatus(@RequestParam Long campaignId, @RequestParam Integer status) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            campaign.setTrangThai(status);
            campaignRepository.save(campaign);
        });
        return "redirect:/admin/campaigns";
    }
    
    @GetMapping("/{campaignId}/stores")
    public String getStoreAllocationModal(@PathVariable Long campaignId, Model model) {
        // Find all active stores
        List<Store> activeStores = storeRepository.findByTrangThai(1);
        
        // Find currently assigned stores
        List<Long> assignedStoreIds = campaignStoreRepository.findByIdChienDich(campaignId)
                .stream()
                .map(CampaignStore::getIdCuaHang)
                .collect(Collectors.toList());
                
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("activeStores", activeStores);
        model.addAttribute("assignedStoreIds", assignedStoreIds);
        
        // Return a fragment HTML to be injected into the modal body
        return "admin/fragments/store-allocation-fragment :: content";
    }
    
    @PostMapping("/{campaignId}/stores/save")
    public String saveStoreAllocation(@PathVariable Long campaignId, @RequestParam(required = false) List<Long> storeIds) {
        // Delete old assignments
        campaignStoreRepository.deleteByIdChienDich(campaignId);
        
        // Save new ones
        if (storeIds != null) {
            for (Long storeId : storeIds) {
                CampaignStore mapping = new CampaignStore();
                mapping.setIdChienDich(campaignId);
                mapping.setIdCuaHang(storeId);
                campaignStoreRepository.save(mapping);
            }
        }
        
        return "redirect:/admin/campaigns";
    }

    @GetMapping("/{campaignId}/rules")
    public String getCampaignRulesModal(@PathVariable Long campaignId, Model model) {
        CampaignRule rule = campaignRuleRepository.findByIdChienDich(campaignId).orElse(new CampaignRule());
        List<CampaignRulePayment> payments = campaignRulePaymentRepository.findByIdChienDich(campaignId);
        List<CampaignRuleSku> skus = campaignRuleSkuRepository.findByIdChienDich(campaignId);
        
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("basicRule", rule);
        model.addAttribute("paymentRules", payments);
        model.addAttribute("skuRules", skus);
        
        return "admin/fragments/campaign-rules-fragment :: content";
    }

    @PostMapping("/{campaignId}/rules/save")
    public String saveCampaignRules(@PathVariable Long campaignId, @ModelAttribute CampaignRuleForm form, RedirectAttributes redirectAttributes) {
        try {
            // 1. Delete old rules
            campaignRuleRepository.deleteByIdChienDich(campaignId);
            campaignRulePaymentRepository.deleteByIdChienDich(campaignId);
            campaignRuleSkuRepository.deleteByIdChienDich(campaignId);
            
            // 2. Save Basic Rule
            if (form.getGiaTriDonHangToiThieu() != null) {
                CampaignRule rule = new CampaignRule();
                rule.setIdChienDich(campaignId);
                rule.setGiaTriDonHangToiThieu(form.getGiaTriDonHangToiThieu());
                campaignRuleRepository.save(rule);
            }
            
            // 3. Save Payment Rules
            if (form.getPaymentMethods() != null && form.getPaymentTurns() != null) {
                for (int i = 0; i < form.getPaymentMethods().size(); i++) {
                    String method = form.getPaymentMethods().get(i);
                    Integer turn = form.getPaymentTurns().get(i);
                    if (method != null && !method.trim().isEmpty() && turn != null) {
                        CampaignRulePayment payment = new CampaignRulePayment();
                        payment.setIdChienDich(campaignId);
                        payment.setPhuongThucThanhToan(method);
                        payment.setSoLuotThuong(turn);
                        campaignRulePaymentRepository.save(payment);
                    }
                }
            }
            
            // 4. Save SKU Rules
            if (form.getSkuCodes() != null && form.getSkuTurns() != null) {
                for (int i = 0; i < form.getSkuCodes().size(); i++) {
                    String sku = form.getSkuCodes().get(i);
                    Integer turn = form.getSkuTurns().get(i);
                    if (sku != null && !sku.trim().isEmpty() && turn != null) {
                        CampaignRuleSku ruleSku = new CampaignRuleSku();
                        ruleSku.setIdChienDich(campaignId);
                        ruleSku.setMaSku(sku);
                        ruleSku.setSoLuotThuong(turn);
                        campaignRuleSkuRepository.save(ruleSku);
                    }
                }
            }
        } catch (Exception e) {
            String errorMsg = "Đã xảy ra lỗi khi lưu cấu hình luật.";
            if (e.getCause() != null && e.getCause().getCause() != null) {
                errorMsg = e.getCause().getCause().getMessage();
            }
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        }
        
        return "redirect:/admin/campaigns";
    }
}
