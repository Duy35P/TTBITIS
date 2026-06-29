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
import com.bitis.luckydraw.repository.SystemAuditLogRepository;
import com.bitis.luckydraw.model.SystemAuditLog;
import com.bitis.luckydraw.dto.CampaignRuleForm;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/admin/campaigns")
public class AdminCampaignController {

    private final CampaignRepository campaignRepository;
    private final StoreRepository storeRepository;
    private final CampaignStoreRepository campaignStoreRepository;
    private final CampaignRuleRepository campaignRuleRepository;
    private final CampaignRulePaymentRepository campaignRulePaymentRepository;
    private final CampaignRuleSkuRepository campaignRuleSkuRepository;
    private final SystemAuditLogRepository systemAuditLogRepository;
    private final JdbcTemplate jdbcTemplate;

    public AdminCampaignController(CampaignRepository campaignRepository, StoreRepository storeRepository, CampaignStoreRepository campaignStoreRepository,
                                   CampaignRuleRepository campaignRuleRepository, CampaignRulePaymentRepository campaignRulePaymentRepository, CampaignRuleSkuRepository campaignRuleSkuRepository,
                                   SystemAuditLogRepository systemAuditLogRepository, JdbcTemplate jdbcTemplate) {
        this.campaignRepository = campaignRepository;
        this.storeRepository = storeRepository;
        this.campaignStoreRepository = campaignStoreRepository;
        this.campaignRuleRepository = campaignRuleRepository;
        this.campaignRulePaymentRepository = campaignRulePaymentRepository;
        this.campaignRuleSkuRepository = campaignRuleSkuRepository;
        this.systemAuditLogRepository = systemAuditLogRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public String listCampaigns(Model model) {
        model.addAttribute("campaigns", campaignRepository.findAll());
        return "admin/campaign-list";
    }

    @PostMapping("/save")
    @Transactional
    public String saveCampaign(@ModelAttribute Campaign formCampaign, org.springframework.validation.BindingResult bindingResult, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu nhập vào không hợp lệ. Vui lòng kiểm tra lại định dạng ngày giờ (chuẩn: dd/MM/yyyy HH:mm) hoặc các trường bắt buộc!");
            return "redirect:/admin/campaigns";
        }
        
        // Kiểm tra logic Ngày bắt đầu và Ngày kết thúc
        if (formCampaign.getNgayBatDau() != null && formCampaign.getNgayKetThuc() != null) {
            if (formCampaign.getNgayBatDau().isAfter(formCampaign.getNgayKetThuc())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Ngày bắt đầu không thể diễn ra sau ngày kết thúc!");
                return "redirect:/admin/campaigns";
            }
        }
        // Kiểm tra trùng lặp Mã Chiến Dịch
        if (formCampaign.getId() == null) {
            if (formCampaign.getMaChienDich() != null && !formCampaign.getMaChienDich().trim().isEmpty()) {
                if (campaignRepository.findByMaChienDich(formCampaign.getMaChienDich()).isPresent()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Mã chiến dịch '" + formCampaign.getMaChienDich() + "' đã tồn tại!");
                    return "redirect:/admin/campaigns";
                }
            }
        } else {
            Campaign existingById = campaignRepository.findById(formCampaign.getId()).orElse(null);
            if (existingById != null && formCampaign.getMaChienDich() != null && !existingById.getMaChienDich().equals(formCampaign.getMaChienDich())) {
                if (campaignRepository.findByMaChienDich(formCampaign.getMaChienDich()).isPresent()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Mã chiến dịch '" + formCampaign.getMaChienDich() + "' đã tồn tại!");
                    return "redirect:/admin/campaigns";
                }
            }
        }

        // Kiểm tra trùng lặp Đường dẫn Slug
        if (formCampaign.getDuongDanSlug() != null && !formCampaign.getDuongDanSlug().trim().isEmpty()) {
            java.util.Optional<Campaign> existingSlug = campaignRepository.findByDuongDanSlug(formCampaign.getDuongDanSlug());
            if (existingSlug.isPresent() && (formCampaign.getId() == null || !existingSlug.get().getId().equals(formCampaign.getId()))) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Đường dẫn Slug '" + formCampaign.getDuongDanSlug() + "' đã được sử dụng!");
                return "redirect:/admin/campaigns";
            }
        }
        
        try {
            Campaign campaign;
            String oldMaChienDich = null;
            if (formCampaign.getId() != null) {
                campaign = campaignRepository.findById(formCampaign.getId()).orElse(new Campaign());
                oldMaChienDich = campaign.getMaChienDich();
                campaign.setMaChienDich(formCampaign.getMaChienDich());
                campaign.setTenChienDich(formCampaign.getTenChienDich());
                campaign.setNgayBatDau(formCampaign.getNgayBatDau());
                campaign.setNgayKetThuc(formCampaign.getNgayKetThuc());
                campaign.setDuongDanSlug(formCampaign.getDuongDanSlug());
                if (formCampaign.getTrangThai() != null) {
                    campaign.setTrangThai(formCampaign.getTrangThai());
                }
                campaign.setMoTa(formCampaign.getMoTa());
            } else {
                campaign = formCampaign;
                campaign.setTrangThai(0); // Luôn luôn tạm ngưng khi mới tạo
            }
            campaignRepository.save(campaign);
            
            // Cascade update ma_chien_dich for all related tables
            if (oldMaChienDich != null && !oldMaChienDich.equals(campaign.getMaChienDich())) {
                String newMaChienDich = campaign.getMaChienDich();
                String[] relatedTables = {
                    "campaign_store", "campaign_rule", "campaign_rule_payment", 
                    "campaign_rule_sku", "customer_turn", "prize", "turn_transaction"
                };
                for (String table : relatedTables) {
                    jdbcTemplate.update("UPDATE " + table + " SET ma_chien_dich = ? WHERE ma_chien_dich = ?", newMaChienDich, oldMaChienDich);
                }
            }
            
            SystemAuditLog log = new SystemAuditLog();
            log.setStaffId(1L); // TODO: Get from auth
            log.setActionType(formCampaign.getId() != null ? "UPDATE" : "CREATE");
            log.setTargetTable("campaign");
            log.setTargetRecordId(campaign.getMaChienDich());
            log.setDescription(formCampaign.getId() != null ? "Chỉnh sửa thông tin chiến dịch" : "Tạo mới chiến dịch");
            log.setIpAddress("127.0.0.1"); // TODO: Get actual IP if needed
            systemAuditLogRepository.save(log);
        } catch (Exception e) {
            String errorMsg = e.getCause() != null && e.getCause().getCause() != null 
                ? e.getCause().getCause().getMessage() 
                : e.getMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        }
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/save-ajax")
    @ResponseBody
    @Transactional
    public java.util.Map<String, Object> saveCampaignAjax(@ModelAttribute Campaign formCampaign, org.springframework.validation.BindingResult bindingResult) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu nhập vào không hợp lệ. Vui lòng kiểm tra lại định dạng ngày giờ (chuẩn: dd/MM/yyyy HH:mm) hoặc các trường bắt buộc!");
            return response;
        }
        
        // Kiểm tra logic Ngày bắt đầu và Ngày kết thúc
        if (formCampaign.getNgayBatDau() != null && formCampaign.getNgayKetThuc() != null) {
            if (formCampaign.getNgayBatDau().isAfter(formCampaign.getNgayKetThuc())) {
                response.put("success", false);
                response.put("message", "Lỗi: Ngày bắt đầu không thể diễn ra sau ngày kết thúc!");
                return response;
            }
        }
        
        // Kiểm tra trùng lặp Mã Chiến Dịch
        if (formCampaign.getId() == null) {
            if (formCampaign.getMaChienDich() != null && !formCampaign.getMaChienDich().trim().isEmpty()) {
                if (campaignRepository.findByMaChienDich(formCampaign.getMaChienDich()).isPresent()) {
                    response.put("success", false);
                    response.put("message", "Lỗi: Mã chiến dịch '" + formCampaign.getMaChienDich() + "' đã tồn tại!");
                    return response;
                }
            }
        } else {
            Campaign existingById = campaignRepository.findById(formCampaign.getId()).orElse(null);
            if (existingById != null && formCampaign.getMaChienDich() != null && !existingById.getMaChienDich().equals(formCampaign.getMaChienDich())) {
                if (campaignRepository.findByMaChienDich(formCampaign.getMaChienDich()).isPresent()) {
                    response.put("success", false);
                    response.put("message", "Lỗi: Mã chiến dịch '" + formCampaign.getMaChienDich() + "' đã tồn tại!");
                    return response;
                }
            }
        }

        // Kiểm tra trùng lặp Đường dẫn Slug
        if (formCampaign.getDuongDanSlug() != null && !formCampaign.getDuongDanSlug().trim().isEmpty()) {
            java.util.Optional<Campaign> existingSlug = campaignRepository.findByDuongDanSlug(formCampaign.getDuongDanSlug());
            if (existingSlug.isPresent() && (formCampaign.getId() == null || !existingSlug.get().getId().equals(formCampaign.getId()))) {
                response.put("success", false);
                response.put("message", "Lỗi: Đường dẫn Slug '" + formCampaign.getDuongDanSlug() + "' đã được sử dụng!");
                return response;
            }
        }
        
        try {
            Campaign campaign;
            String oldMaChienDich = null;
            if (formCampaign.getId() != null) {
                campaign = campaignRepository.findById(formCampaign.getId()).orElse(new Campaign());
                oldMaChienDich = campaign.getMaChienDich();
                campaign.setMaChienDich(formCampaign.getMaChienDich());
                campaign.setTenChienDich(formCampaign.getTenChienDich());
                campaign.setNgayBatDau(formCampaign.getNgayBatDau());
                campaign.setNgayKetThuc(formCampaign.getNgayKetThuc());
                campaign.setDuongDanSlug(formCampaign.getDuongDanSlug());
                if (formCampaign.getTrangThai() != null) {
                    campaign.setTrangThai(formCampaign.getTrangThai());
                }
                campaign.setMoTa(formCampaign.getMoTa());
            } else {
                campaign = formCampaign;
                campaign.setTrangThai(0); // Luôn luôn tạm ngưng khi mới tạo
            }
            campaignRepository.save(campaign);
            
            // Cascade update ma_chien_dich for all related tables
            if (oldMaChienDich != null && !oldMaChienDich.equals(campaign.getMaChienDich())) {
                String newMaChienDich = campaign.getMaChienDich();
                String[] relatedTables = {
                    "campaign_store", "campaign_rule", "campaign_rule_payment", 
                    "campaign_rule_sku", "customer_turn", "prize", "turn_transaction"
                };
                for (String table : relatedTables) {
                    jdbcTemplate.update("UPDATE " + table + " SET ma_chien_dich = ? WHERE ma_chien_dich = ?", newMaChienDich, oldMaChienDich);
                }
            }
            
            SystemAuditLog log = new SystemAuditLog();
            log.setStaffId(1L); // TODO: Get from auth
            log.setActionType(formCampaign.getId() != null ? "UPDATE" : "CREATE");
            log.setTargetTable("campaign");
            log.setTargetRecordId(campaign.getMaChienDich());
            log.setDescription(formCampaign.getId() != null ? "Chỉnh sửa thông tin chiến dịch" : "Tạo mới chiến dịch");
            log.setIpAddress("127.0.0.1"); // TODO: Get actual IP if needed
            systemAuditLogRepository.save(log);
            
            response.put("success", true);
        } catch (Exception e) {
            String errorMsg = e.getCause() != null && e.getCause().getCause() != null 
                ? e.getCause().getCause().getMessage() 
                : e.getMessage();
            response.put("success", false);
            response.put("message", "Lỗi máy chủ: " + errorMsg);
        }
        return response;
    }

    @PostMapping("/toggle-status")
    public String toggleStatus(@RequestParam Long campaignId, @RequestParam Integer status, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            if (status == 1) { // Đang yêu cầu Kích hoạt
                if (campaign.getNgayBatDau() != null && campaign.getNgayBatDau().isAfter(java.time.LocalDateTime.now())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể kích hoạt vì chưa đến ngày bắt đầu chiến dịch.");
                    return;
                }
                
                String maChienDich = campaign.getMaChienDich();
                boolean hasRules = campaignRuleRepository.findByMaChienDich(maChienDich).isPresent() ||
                                   !campaignRulePaymentRepository.findByMaChienDich(maChienDich).isEmpty() ||
                                   !campaignRuleSkuRepository.findByMaChienDich(maChienDich).isEmpty();
                
                if (!hasRules) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể kích hoạt vì chiến dịch chưa được cấu hình luật chơi (Basic, SKU, hoặc Payment).");
                    return;
                }
            }
            campaign.setTrangThai(status);
            campaignRepository.save(campaign);
            
            SystemAuditLog log = new SystemAuditLog();
            log.setStaffId(1L); // TODO: Get from auth
            log.setActionType("UPDATE");
            log.setTargetTable("campaign");
            log.setTargetRecordId(campaign.getMaChienDich());
            log.setDescription(status == 1 ? "Yêu cầu kích hoạt chiến dịch" : "Tạm ngưng chiến dịch");
            log.setIpAddress("127.0.0.1"); // TODO: Get actual IP if needed
            systemAuditLogRepository.save(log);
        });
        return "redirect:/admin/campaigns";
    }
    
    @GetMapping("/{campaignId}/history")
    public String getCampaignHistoryModal(@PathVariable Long campaignId, Model model) {
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
        List<SystemAuditLog> historyList = systemAuditLogRepository.findByTargetTableAndTargetRecordIdOrderByIdDesc("campaign", campaign.getMaChienDich());
        
        model.addAttribute("campaign", campaign);
        model.addAttribute("historyList", historyList);
        
        return "admin/fragments/campaign-history-fragment :: content";
    }

    @GetMapping("/{campaignId}/stores")
    public String getStoreAllocationModal(@PathVariable Long campaignId, Model model) {
        // Find all active stores
        List<Store> activeStores = storeRepository.findByTrangThai(1);
        
        // Find currently assigned stores
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
        List<String> assignedStoreMas = campaignStoreRepository.findByMaChienDich(campaign.getMaChienDich())
                .stream()
                .map(CampaignStore::getMaStore)
                .collect(Collectors.toList());
                
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("activeStores", activeStores);
        model.addAttribute("assignedStoreMas", assignedStoreMas);
        
        // Return a fragment HTML to be injected into the modal body
        return "admin/fragments/store-allocation-fragment :: content";
    }
    
    @PostMapping("/{campaignId}/stores/save")
    public String saveStoreAllocation(@PathVariable Long campaignId, @RequestParam(required = false) List<String> storeMas) {
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
        String maChienDich = campaign.getMaChienDich();
        // Delete old assignments
        campaignStoreRepository.deleteByMaChienDich(maChienDich);
        
        // Save new ones
        if (storeMas != null) {
            for (String storeMa : storeMas) {
                CampaignStore mapping = new CampaignStore();
                mapping.setMaChienDich(maChienDich);
                mapping.setMaStore(storeMa);
                campaignStoreRepository.save(mapping);
            }
        }
        
        return "redirect:/admin/campaigns";
    }

    @GetMapping("/{campaignId}/rules")
    public String getCampaignRulesModal(@PathVariable Long campaignId, Model model) {
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
        String maChienDich = campaign.getMaChienDich();
        CampaignRule rule = campaignRuleRepository.findByMaChienDich(maChienDich).orElse(new CampaignRule());
        List<CampaignRulePayment> payments = campaignRulePaymentRepository.findByMaChienDich(maChienDich);
        List<CampaignRuleSku> skus = campaignRuleSkuRepository.findByMaChienDich(maChienDich);
        
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("basicRule", rule);
        model.addAttribute("paymentRules", payments);
        model.addAttribute("skuRules", skus);
        
        return "admin/fragments/campaign-rules-fragment :: content";
    }

    @PostMapping("/{campaignId}/rules/save")
    public String saveCampaignRules(@PathVariable Long campaignId, @ModelAttribute CampaignRuleForm form, RedirectAttributes redirectAttributes) {
        try {
            Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
            String maChienDich = campaign.getMaChienDich();
            // 1. Delete old rules
            campaignRuleRepository.deleteByMaChienDich(maChienDich);
            campaignRulePaymentRepository.deleteByMaChienDich(maChienDich);
            campaignRuleSkuRepository.deleteByMaChienDich(maChienDich);
            
            // 2. Save Basic Rule
            if (form.getGiaTriDonHangToiThieu() != null) {
                CampaignRule rule = new CampaignRule();
                rule.setMaChienDich(maChienDich);
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
                        payment.setMaChienDich(maChienDich);
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
                        ruleSku.setMaChienDich(maChienDich);
                        ruleSku.setMaSku(sku);
                        ruleSku.setSoLuotThuong(turn);
                        campaignRuleSkuRepository.save(ruleSku);
                    }
                }
            }
            
            SystemAuditLog log = new SystemAuditLog();
            log.setStaffId(1L); // TODO: Get from auth
            log.setActionType("UPDATE");
            log.setTargetTable("campaign");
            log.setTargetRecordId(maChienDich);
            log.setDescription("Cập nhật luật ưu đãi (SKU, Payment, Tối thiểu)");
            log.setIpAddress("127.0.0.1");
            systemAuditLogRepository.save(log);
            
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
