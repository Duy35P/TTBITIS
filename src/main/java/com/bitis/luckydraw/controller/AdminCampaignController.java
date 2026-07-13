package com.bitis.luckydraw.controller;

import org.springframework.security.access.prepost.PreAuthorize;
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

import com.bitis.luckydraw.model.Prize;

import com.bitis.luckydraw.repository.StoreRepository;

import com.bitis.luckydraw.repository.CampaignStoreRepository;

import com.bitis.luckydraw.repository.CampaignRuleRepository;

import com.bitis.luckydraw.repository.CampaignRulePaymentRepository;

import com.bitis.luckydraw.repository.CampaignRuleSkuRepository;

import com.bitis.luckydraw.repository.PrizeRepository;

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
    private final PrizeRepository prizeRepository;

    public AdminCampaignController(CampaignRepository campaignRepository, StoreRepository storeRepository, CampaignStoreRepository campaignStoreRepository,
                                   CampaignRuleRepository campaignRuleRepository, CampaignRulePaymentRepository campaignRulePaymentRepository, CampaignRuleSkuRepository campaignRuleSkuRepository,
                                   SystemAuditLogRepository systemAuditLogRepository, JdbcTemplate jdbcTemplate, PrizeRepository prizeRepository) {
        this.campaignRepository = campaignRepository;
        this.storeRepository = storeRepository;
        this.campaignStoreRepository = campaignStoreRepository;
        this.campaignRuleRepository = campaignRuleRepository;
        this.campaignRulePaymentRepository = campaignRulePaymentRepository;
        this.campaignRuleSkuRepository = campaignRuleSkuRepository;
        this.systemAuditLogRepository = systemAuditLogRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.prizeRepository = prizeRepository;
    }

    @GetMapping
    public String listCampaigns(Model model, org.springframework.security.core.Authentication auth) {
        model.addAttribute("campaigns", campaignRepository.findAll());
        return "admin/campaign-list";
    }

    @GetMapping("/api/statuses")
    @ResponseBody
    public java.util.Map<Long, Integer> getCampaignStatuses() {
        java.util.Map<Long, Integer> map = new java.util.HashMap<>();
        campaignRepository.findAll().forEach(c -> map.put(c.getId(), c.getTrangThai()));
        return map;
    }

    private void processCampaignSave(Campaign formCampaign, org.springframework.validation.BindingResult bindingResult) throws IllegalArgumentException {
        boolean isNew = formCampaign.getId() == null;

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException("Dữ liệu nhập vào không hợp lệ. Vui lòng kiểm tra lại định dạng ngày giờ (chuẩn: dd/MM/yyyy HH:mm) hoặc các trường bắt buộc!");
        }
        
        if (formCampaign.getNgayBatDau() != null && formCampaign.getNgayKetThuc() != null && formCampaign.getNgayBatDau().isAfter(formCampaign.getNgayKetThuc())) {
            throw new IllegalArgumentException("Lỗi: Ngày bắt đầu không thể diễn ra sau ngày kết thúc!");
        }
        
        if (formCampaign.getHanTokenNgay() != null) {
            if (formCampaign.getHanTokenNgay() < 1) {
                throw new IllegalArgumentException("Lỗi: Hạn sử dụng Token phải từ 1 ngày trở lên!");
            }
            if (formCampaign.getNgayBatDau() != null && formCampaign.getNgayKetThuc() != null) {
                long durationDays = java.time.temporal.ChronoUnit.DAYS.between(formCampaign.getNgayBatDau().toLocalDate(), formCampaign.getNgayKetThuc().toLocalDate());
                if (formCampaign.getHanTokenNgay() > durationDays) {
                    throw new IllegalArgumentException("Lỗi: Hạn sử dụng Token (" + formCampaign.getHanTokenNgay() + " ngày) không được lớn hơn tổng số ngày hoạt động của chiến dịch (" + durationDays + " ngày)!");
                }
            }
        }

        if (formCampaign.getId() != null && formCampaign.getNgayKetThuc() != null) {
            Campaign existingById = campaignRepository.findById(formCampaign.getId()).orElse(null);
            if (existingById != null && existingById.getNgayKetThuc() != null) {
                java.time.LocalDateTime newEnd = formCampaign.getNgayKetThuc().withSecond(0).withNano(0);
                java.time.LocalDateTime oldEnd = existingById.getNgayKetThuc().withSecond(0).withNano(0);
                if (!newEnd.isEqual(oldEnd) && newEnd.isBefore(java.time.LocalDateTime.now())) {
                    throw new IllegalArgumentException("Lỗi: Không thể đổi ngày kết thúc về một thời điểm trong quá khứ!");
                }
            }
        }
        
        String maChienDich = formCampaign.getMaChienDich();
        if (maChienDich != null && !maChienDich.trim().isEmpty()) {
            Campaign existingById = isNew ? null : campaignRepository.findById(formCampaign.getId()).orElse(null);
            if (isNew || (existingById != null && !existingById.getMaChienDich().equals(maChienDich))) {
                if (campaignRepository.findByMaChienDich(maChienDich).isPresent()) {
                    throw new IllegalArgumentException("Lỗi: Mã chiến dịch '" + maChienDich + "' đã tồn tại!");
                }
            }
        }

        if (formCampaign.getDuongDanSlug() != null && !formCampaign.getDuongDanSlug().trim().isEmpty()) {
            campaignRepository.findByDuongDanSlug(formCampaign.getDuongDanSlug()).ifPresent(existingSlug -> {
                if (formCampaign.getId() == null || !existingSlug.getId().equals(formCampaign.getId())) {
                    throw new IllegalArgumentException("Lỗi: Đường dẫn Slug '" + formCampaign.getDuongDanSlug() + "' đã được sử dụng!");
                }
            });
        }
        
        Campaign campaign;
        String oldMaChienDich = null;
        String actionDescription = "Tạo mới chiến dịch";
        if (!isNew) {
            campaign = campaignRepository.findById(formCampaign.getId()).orElse(new Campaign());
            oldMaChienDich = campaign.getMaChienDich();
            actionDescription = getUpdateDescription(campaign, formCampaign);
            
            campaign.setTenChienDich(formCampaign.getTenChienDich());
            campaign.setNgayBatDau(formCampaign.getNgayBatDau());
            campaign.setNgayKetThuc(formCampaign.getNgayKetThuc());
            campaign.setDuongDanSlug(formCampaign.getDuongDanSlug());
            campaign.setDocQuyen(formCampaign.getDocQuyen() != null ? formCampaign.getDocQuyen() : false);
            if (formCampaign.getTrangThai() != null) campaign.setTrangThai(formCampaign.getTrangThai());
            
            if (campaign.getTrangThai() != null && campaign.getTrangThai() == 2 && campaign.getNgayKetThuc() != null && campaign.getNgayKetThuc().isAfter(java.time.LocalDateTime.now())) {
                campaign.setTrangThai(0);
            }
            campaign.setMoTa(formCampaign.getMoTa());
            if (formCampaign.getHanTokenNgay() != null) {
                campaign.setHanTokenNgay(formCampaign.getHanTokenNgay());
            }
        } else {
            campaign = formCampaign;
            campaign.setTrangThai(0);
            if (campaign.getDocQuyen() == null) campaign.setDocQuyen(false);
        }
        campaignRepository.save(campaign);
        
        if (oldMaChienDich != null && !oldMaChienDich.equals(campaign.getMaChienDich())) {
            String newMaChienDich = campaign.getMaChienDich();
            String[] relatedTables = {"campaign_store", "campaign_rule", "campaign_rule_payment", "campaign_rule_sku", "customer_turn", "prize", "turn_transaction"};
            for (String table : relatedTables) jdbcTemplate.update("UPDATE " + table + " SET ma_chien_dich = ? WHERE ma_chien_dich = ?", newMaChienDich, oldMaChienDich);
        }
        
        SystemAuditLog log = new SystemAuditLog();
        log.setStaffId(1L);
        log.setActionType(isNew ? "CREATE" : "UPDATE");
        log.setTargetTable("campaign");
        log.setTargetRecordId(campaign.getMaChienDich());
        log.setDescription(actionDescription);
        log.setIpAddress("127.0.0.1");
        systemAuditLogRepository.save(log);
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CHIENDICH_ADD') or hasAuthority('ACT_CHIENDICH_EDIT')")
    @Transactional
    public String saveCampaign(@ModelAttribute Campaign formCampaign, org.springframework.validation.BindingResult bindingResult, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            processCampaignSave(formCampaign, bindingResult);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            String errorMsg = e.getCause() != null && e.getCause().getCause() != null ? e.getCause().getCause().getMessage() : e.getMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        }
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/save-ajax")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CHIENDICH_ADD') or hasAuthority('ACT_CHIENDICH_EDIT')")
    @Transactional
    public java.util.Map<String, Object> saveCampaignAjax(@ModelAttribute Campaign formCampaign, org.springframework.validation.BindingResult bindingResult) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            processCampaignSave(formCampaign, bindingResult);
            response.put("success", true);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            String errorMsg = e.getCause() != null && e.getCause().getCause() != null ? e.getCause().getCause().getMessage() : e.getMessage();
            response.put("success", false);
            response.put("message", "Lỗi máy chủ: " + errorMsg);
        }
        return response;
    }

    @PostMapping("/toggle-status")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CHIENDICH_EDIT')")
    public String toggleStatus(@RequestParam Long campaignId, @RequestParam Integer status, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            if (status == 1) { // Đang yêu cầu Kích hoạt
                String maChienDich = campaign.getMaChienDich();
                boolean hasRules = campaignRuleRepository.findByMaChienDich(maChienDich).isPresent() ||
                                   !campaignRulePaymentRepository.findByMaChienDich(maChienDich).isEmpty() ||
                                   !campaignRuleSkuRepository.findByMaChienDich(maChienDich).isEmpty();
                
                if (!hasRules) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể kích hoạt vì chiến dịch chưa được cấu hình luật chơi (Basic, SKU, hoặc Payment).");
                    return;
                }
                
                if (campaign.getNgayKetThuc() != null && campaign.getNgayKetThuc().isBefore(java.time.LocalDateTime.now())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể kích hoạt vì chiến dịch đã qua ngày kết thúc.");
                    return;
                }
                if (campaign.getNgayBatDau() != null && campaign.getNgayBatDau().isAfter(java.time.LocalDateTime.now())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể kích hoạt vì chưa đến ngày bắt đầu chiến dịch.");
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CHIENDICH_EDIT')")
    public String saveStoreAllocation(@PathVariable Long campaignId, @RequestParam(required = false) List<String> storeMas, RedirectAttributes redirectAttributes) {
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
        String maChienDich = campaign.getMaChienDich();
        
        List<CampaignStore> oldStores = campaignStoreRepository.findByMaChienDich(maChienDich);
        java.util.Set<String> oldStoreMas = oldStores.stream().map(CampaignStore::getMaStore).collect(Collectors.toSet());
        
        java.util.Set<String> newStoreMas = new java.util.HashSet<>();
        if (storeMas != null) {
            newStoreMas.addAll(storeMas);
        }
        
        java.util.Set<String> addedStores = new java.util.HashSet<>(newStoreMas);
        addedStores.removeAll(oldStoreMas);
        
        java.util.Set<String> removedStores = new java.util.HashSet<>(oldStoreMas);
        removedStores.removeAll(newStoreMas);

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
        
        if (!addedStores.isEmpty() || !removedStores.isEmpty()) {
            StringBuilder desc = new StringBuilder("Thay đổi phân bổ cửa hàng:");
            if (!addedStores.isEmpty()) {
                desc.append(" thêm ");
                desc.append(formatStoreList(addedStores));
            }
            if (!removedStores.isEmpty()) {
                if (!addedStores.isEmpty()) desc.append(",");
                desc.append(" gỡ bỏ ");
                desc.append(formatStoreList(removedStores));
            }
            
            SystemAuditLog log = new SystemAuditLog();
            log.setStaffId(1L); // TODO: Get from auth
            log.setActionType("UPDATE");
            log.setTargetTable("campaign");
            log.setTargetRecordId(maChienDich);
            log.setDescription(desc.toString());
            log.setIpAddress("127.0.0.1");
            systemAuditLogRepository.save(log);
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CHIENDICH_EDIT')")
    public String saveCampaignRules(@PathVariable Long campaignId, @ModelAttribute CampaignRuleForm form, RedirectAttributes redirectAttributes) {
        try {
            Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
            String maChienDich = campaign.getMaChienDich();
            
            // Fetch old rules for comparison
            CampaignRule oldBasicRule = campaignRuleRepository.findByMaChienDich(maChienDich).orElse(null);
            List<CampaignRulePayment> oldPayments = campaignRulePaymentRepository.findByMaChienDich(maChienDich);
            List<CampaignRuleSku> oldSkus = campaignRuleSkuRepository.findByMaChienDich(maChienDich);
            
            Double oldMinOrder = oldBasicRule != null ? oldBasicRule.getGiaTriDonHangToiThieu() : null;
            Double newMinOrder = form.getGiaTriDonHangToiThieu();
            boolean basicChanged = !java.util.Objects.equals(oldMinOrder, newMinOrder);
            
            java.util.Map<String, Integer> oldPaymentMap = new java.util.HashMap<>();
            if (oldPayments != null) {
                for (CampaignRulePayment p : oldPayments) {
                    oldPaymentMap.put(p.getPhuongThucThanhToan(), p.getSoLuotThuong());
                }
            }
            java.util.Map<String, Integer> newPaymentMap = new java.util.HashMap<>();
            if (form.getPaymentMethods() != null && form.getPaymentTurns() != null) {
                for (int i = 0; i < form.getPaymentMethods().size(); i++) {
                    String method = form.getPaymentMethods().get(i);
                    Integer turn = form.getPaymentTurns().get(i);
                    if (method != null && !method.trim().isEmpty() && turn != null) {
                        newPaymentMap.put(method.trim(), turn);
                    }
                }
            }
            boolean paymentChanged = !oldPaymentMap.equals(newPaymentMap);
            
            java.util.Map<String, Integer> oldSkuMap = new java.util.HashMap<>();
            if (oldSkus != null) {
                for (CampaignRuleSku s : oldSkus) {
                    oldSkuMap.put(s.getMaSku(), s.getSoLuotThuong());
                }
            }
            java.util.Map<String, Integer> newSkuMap = new java.util.HashMap<>();
            if (form.getSkuCodes() != null && form.getSkuTurns() != null) {
                for (int i = 0; i < form.getSkuCodes().size(); i++) {
                    String sku = form.getSkuCodes().get(i);
                    Integer turn = form.getSkuTurns().get(i);
                    if (sku != null && !sku.trim().isEmpty() && turn != null) {
                        newSkuMap.put(sku.trim(), turn);
                    }
                }
            }
            boolean skuChanged = !oldSkuMap.equals(newSkuMap);

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
            for (java.util.Map.Entry<String, Integer> entry : newPaymentMap.entrySet()) {
                CampaignRulePayment payment = new CampaignRulePayment();
                payment.setMaChienDich(maChienDich);
                payment.setPhuongThucThanhToan(entry.getKey());
                payment.setSoLuotThuong(entry.getValue());
                campaignRulePaymentRepository.save(payment);
            }
            
            // 4. Save SKU Rules
            for (java.util.Map.Entry<String, Integer> entry : newSkuMap.entrySet()) {
                CampaignRuleSku ruleSku = new CampaignRuleSku();
                ruleSku.setMaChienDich(maChienDich);
                ruleSku.setMaSku(entry.getKey());
                ruleSku.setSoLuotThuong(entry.getValue());
                campaignRuleSkuRepository.save(ruleSku);
            }
            
            List<String> updatedRules = new java.util.ArrayList<>();
            if (basicChanged) updatedRules.add("giá trị đơn hàng tối thiểu");
            if (paymentChanged) updatedRules.add("phương thức thanh toán");
            if (skuChanged) updatedRules.add("sản phẩm (SKU)");
            
            if (!updatedRules.isEmpty()) {
                String ruleDesc = "Chỉnh sửa luật ưu đãi: cập nhật " + String.join(", ", updatedRules) + " của chiến dịch";
                if (newMinOrder == null && newPaymentMap.isEmpty() && newSkuMap.isEmpty()) {
                    ruleDesc = "Xóa toàn bộ luật ưu đãi của chiến dịch";
                }
                
                SystemAuditLog log = new SystemAuditLog();
                log.setStaffId(1L); // TODO: Get from auth
                log.setActionType("UPDATE");
                log.setTargetTable("campaign");
                log.setTargetRecordId(maChienDich);
                log.setDescription(ruleDesc);
                log.setIpAddress("127.0.0.1");
                systemAuditLogRepository.save(log);
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

    @GetMapping("/{campaignId}/design")
    public String designMinigame(@PathVariable Long campaignId, Model model) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chiến dịch"));
        
        List<Prize> prizes = prizeRepository.findByMaChienDich(campaign.getMaChienDich());
        
        model.addAttribute("campaign", campaign);
        model.addAttribute("prizes", prizes);
        return "admin/minigame-builder";
    }

    @PostMapping("/design/save")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CHIENDICH_EDIT')")
    public org.springframework.http.ResponseEntity<?> saveDesign(@RequestBody java.util.Map<String, String> payload) {
        try {
            Long campaignId = Long.parseLong(payload.get("campaignId"));
            String slug = payload.get("slug");
            String configJson = payload.get("configJson");
            String bannerUrl = payload.get("bannerUrl");
            
            Campaign campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chiến dịch"));
            
            campaign.setDuongDanSlug(slug);
            campaign.setCauhinhThemeJson(configJson);
            if (bannerUrl != null && !bannerUrl.isEmpty()) {
                campaign.setHinhAnhUrl(bannerUrl);
            }
            
            campaignRepository.save(campaign);
            
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status", "success"));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Từ khóa đường dẫn (Slug) đã tồn tại. Vui lòng chọn từ khóa khác."));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    private String getUpdateDescription(Campaign existing, Campaign newValues) {
        List<String> changes = new java.util.ArrayList<>();
        if (!java.util.Objects.equals(existing.getMaChienDich(), newValues.getMaChienDich())) {
            changes.add("mã chiến dịch");
        }
        if (!java.util.Objects.equals(existing.getTenChienDich(), newValues.getTenChienDich())) {
            changes.add("tên chiến dịch");
        }
        if (!java.util.Objects.equals(existing.getNgayBatDau(), newValues.getNgayBatDau())) {
            changes.add("ngày bắt đầu");
        }
        if (!java.util.Objects.equals(existing.getNgayKetThuc(), newValues.getNgayKetThuc())) {
            changes.add("ngày kết thúc");
        }
        if (!java.util.Objects.equals(existing.getDuongDanSlug(), newValues.getDuongDanSlug())) {
            changes.add("đường dẫn slug");
        }
        if (newValues.getTrangThai() != null && !java.util.Objects.equals(existing.getTrangThai(), newValues.getTrangThai())) {
            changes.add("trạng thái");
        }
        if (!java.util.Objects.equals(existing.getMoTa(), newValues.getMoTa())) {
            changes.add("mô tả");
        }
        
        Boolean oldDocQuyen = existing.getDocQuyen() != null ? existing.getDocQuyen() : false;
        Boolean newDocQuyen = newValues.getDocQuyen() != null ? newValues.getDocQuyen() : false;
        if (!java.util.Objects.equals(oldDocQuyen, newDocQuyen)) {
            changes.add("thiết lập độc quyền");
        }
        
        if (!java.util.Objects.equals(existing.getHanTokenNgay(), newValues.getHanTokenNgay())) {
            changes.add("hạn token");
        }
        
        if (changes.isEmpty()) {
            return "Chỉnh sửa thông tin chiến dịch";
        }
        return "Chỉnh sửa " + String.join(", ", changes) + " của chiến dịch";
    }

    private String formatStoreList(java.util.Set<String> stores) {
        List<String> list = new java.util.ArrayList<>(stores);
        if (list.size() <= 5) {
            return String.join(", ", list);
        } else {
            return String.join(", ", list.subList(0, 5)) + " và " + (list.size() - 5) + " cửa hàng khác";
        }
    }

    @GetMapping("/export-excel")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportExcel(jakarta.servlet.http.HttpServletResponse response) {
        try {
            List<Campaign> campaigns = campaignRepository.findAll();
            String[] headers = {"Mã Chiến Dịch", "Tên Chiến Dịch", "Ngày Bắt Đầu", "Ngày Kết Thúc", "Trạng Thái"};
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            List<String[]> data = campaigns.stream().map(c -> new String[]{
                c.getMaChienDich(),
                c.getTenChienDich(),
                c.getNgayBatDau() != null ? c.getNgayBatDau().format(formatter) : "Chưa thiết lập",
                c.getNgayKetThuc() != null ? c.getNgayKetThuc().format(formatter) : "Chưa thiết lập",
                c.getTrangThai() != null && c.getTrangThai() == 1 ? "Hoạt động" : "Tạm ngưng"
            }).collect(java.util.stream.Collectors.toList());
            
            com.bitis.luckydraw.util.ExcelExportUtil.exportDataToExcel(response, "DanhSachChienDich", headers, data);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
