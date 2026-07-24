package com.bitis.luckydraw.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.bitis.luckydraw.model.*;
import com.bitis.luckydraw.repository.*;
import com.bitis.luckydraw.dto.CampaignRuleForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/quanly/campaigns")
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

    // Helper: Lấy lỗi nguyên thủy
    private String extractErrorMessage(Exception e) {
        return (e.getCause() != null && e.getCause().getCause() != null) ? e.getCause().getCause().getMessage() : e.getMessage();
    }

    // Helper: Ghi log hệ thống
    private void logAudit(String actionType, String targetRecordId, String description) {
        SystemAuditLog log = new SystemAuditLog();
        log.setStaffId(1L); // TODO: Get from auth
        log.setActionType(actionType);
        log.setTargetTable("campaign");
        log.setTargetRecordId(targetRecordId);
        log.setDescription(description);
        log.setIpAddress("127.0.0.1"); // TODO: Get actual IP if needed
        systemAuditLogRepository.save(log);
    }

    // Helper: Tìm Campaign
    private Campaign getCampaignOrThrow(Long id) {
        return campaignRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chiến dịch"));
    }

    // Helper: Tạo rule map từ form
    private java.util.Map<String, Integer> buildRuleMap(List<String> keys, List<Integer> values) {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        if (keys != null && values != null) {
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                Integer value = (i < values.size()) ? values.get(i) : null;
                if (key != null && !key.trim().isEmpty() && value != null) {
                    map.put(key.trim(), value);
                }
            }
        }
        return map;
    }

    @GetMapping
    public String listCampaigns(Model model, org.springframework.security.core.Authentication auth) {
        model.addAttribute("campaigns", campaignRepository.findAll());
        return "quanly/campaign-list";
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
            if (formCampaign.getHanTokenNgay() < 0) {
                throw new IllegalArgumentException("Lỗi: Hạn sử dụng Token không được là số âm (Nhập 0 để hết hạn theo chiến dịch)!");
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
            if (formCampaign.getSoNgayHienThiThem() != null) {
                campaign.setSoNgayHienThiThem(formCampaign.getSoNgayHienThiThem());
            }
        } else {
            campaign = formCampaign;
            campaign.setTrangThai(0);
            if (campaign.getDocQuyen() == null) campaign.setDocQuyen(false);
        }
        
        if (Boolean.TRUE.equals(campaign.getDocQuyen())) {
            java.util.Set<String> storesToValidate = new java.util.HashSet<>();
            if (!isNew && oldMaChienDich != null) {
                storesToValidate = campaignStoreRepository.findByMaChienDich(oldMaChienDich)
                        .stream().map(CampaignStore::getMaStore).collect(java.util.stream.Collectors.toSet());
            }
            if (!storesToValidate.isEmpty()) {
                validateExclusiveStores(campaign, storesToValidate);
            }
        }
        
        campaignRepository.save(campaign);
        
        if (isNew) {
            Prize losePrize = new Prize();
            losePrize.setMaChienDich(campaign.getMaChienDich());
            losePrize.setMaGiaiThuong("TRUOT-" + System.currentTimeMillis());
            losePrize.setTenGiai("Chúc may mắn lần sau");
            losePrize.setLoaiGiai(0);
            losePrize.setLaGiaiThuong(false);
            losePrize.setXacSuat(0.0);
            losePrize.setGioiHanTrungMoiCustomer(null);
            losePrize.setTonKhoToanHeThong(-1);
            prizeRepository.save(losePrize);
        }
        
        if (oldMaChienDich != null && !oldMaChienDich.equals(campaign.getMaChienDich())) {
            String newMaChienDich = campaign.getMaChienDich();
            String[] relatedTables = {"campaign_store", "campaign_rule", "campaign_rule_payment", "campaign_rule_sku", "customer_turn", "prize", "turn_transaction"};
            for (String table : relatedTables) jdbcTemplate.update("UPDATE " + table + " SET ma_chien_dich = ? WHERE ma_chien_dich = ?", newMaChienDich, oldMaChienDich);
        }
        
        logAudit(isNew ? "CREATE" : "UPDATE", campaign.getMaChienDich(), actionDescription);
    }

    private void validateExclusiveStores(Campaign currentCampaign, java.util.Set<String> storeMasToValidate) throws IllegalArgumentException {
        if (!Boolean.TRUE.equals(currentCampaign.getDocQuyen()) || storeMasToValidate.isEmpty()) {
            return;
        }
        
        List<Campaign> otherExclusives = campaignRepository.findAll().stream()
            .filter(c -> Boolean.TRUE.equals(c.getDocQuyen()))
            .filter(c -> currentCampaign.getId() == null || !c.getId().equals(currentCampaign.getId()))
            .filter(c -> !"Kết thúc".equals(c.getDisplayStatus()) && c.getTrangThai() != 0)
            .collect(java.util.stream.Collectors.toList());
            
        if (otherExclusives.isEmpty()) return;
        
        for (Campaign other : otherExclusives) {
            List<String> otherStores = campaignStoreRepository.findByMaChienDich(other.getMaChienDich())
                .stream().map(CampaignStore::getMaStore).collect(java.util.stream.Collectors.toList());
            
            for (String storeMa : storeMasToValidate) {
                if (otherStores.contains(storeMa)) {
                    Store store = storeRepository.findByMaStore(storeMa).orElse(null);
                    String storeName = store != null ? store.getTenCuaHang() : storeMa;
                    throw new IllegalArgumentException("Lỗi: Cửa hàng '" + storeName + "' đã nằm trong chiến dịch độc quyền khác đang hoạt động (" + other.getTenChienDich() + "). Mỗi cửa hàng chỉ được tham gia 1 chiến dịch độc quyền tại cùng 1 thời điểm!");
                }
            }
        }
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
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
        }
        return "redirect:/quanly/campaigns";
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
            response.put("success", false);
            response.put("message", "Lỗi máy chủ: " + extractErrorMessage(e));
        }
        return response;
    }

    @PostMapping("/toggle-status")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CHIENDICH_EDIT')")
    public String toggleStatus(@RequestParam Long campaignId, @RequestParam Integer status, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Campaign campaign = getCampaignOrThrow(campaignId);
        if (status == 1) { // Đang yêu cầu Kích hoạt
            String maChienDich = campaign.getMaChienDich();
            boolean hasRules = campaignRuleRepository.findByMaChienDich(maChienDich).isPresent() ||
                               !campaignRulePaymentRepository.findByMaChienDich(maChienDich).isEmpty() ||
                               !campaignRuleSkuRepository.findByMaChienDich(maChienDich).isEmpty();
            
            if (!hasRules) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể kích hoạt vì chiến dịch chưa được cấu hình luật chơi (Basic, SKU, hoặc Payment).");
                return "redirect:/quanly/campaigns";
            }
        }
        campaign.setTrangThai(status);
        campaignRepository.save(campaign);
        
        logAudit("UPDATE", campaign.getMaChienDich(), status == 1 ? "Yêu cầu kích hoạt chiến dịch" : "Tạm ngưng chiến dịch");
        return "redirect:/quanly/campaigns";
    }

    @GetMapping("/{campaignId}/history")
    public String getCampaignHistoryModal(@PathVariable Long campaignId, Model model) {
        Campaign campaign = getCampaignOrThrow(campaignId);
        List<SystemAuditLog> historyList = systemAuditLogRepository.findByTargetTableAndTargetRecordIdOrderByIdDesc("campaign", campaign.getMaChienDich());
        
        model.addAttribute("campaign", campaign);
        model.addAttribute("historyList", historyList);
        
        return "quanly/fragments/campaign-history-fragment :: content";
    }

    @GetMapping("/{campaignId}/stores")
    public String getStoreAllocationModal(@PathVariable Long campaignId, Model model) {
        List<Store> activeStores = storeRepository.findByTrangThai(1);
        Campaign campaign = getCampaignOrThrow(campaignId);
        List<String> assignedStoreMas = campaignStoreRepository.findByMaChienDich(campaign.getMaChienDich())
                .stream().map(CampaignStore::getMaStore).collect(Collectors.toList());
                
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("activeStores", activeStores);
        model.addAttribute("assignedStoreMas", assignedStoreMas);
        
        return "quanly/fragments/store-allocation-fragment :: content";
    }
    
    @PostMapping("/{campaignId}/stores/save")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CHIENDICH_EDIT')")
    public String saveStoreAllocation(@PathVariable Long campaignId, @RequestParam(required = false) List<String> storeMas, RedirectAttributes redirectAttributes) {
        Campaign campaign = getCampaignOrThrow(campaignId);
        String maChienDich = campaign.getMaChienDich();
        
        List<CampaignStore> oldStores = campaignStoreRepository.findByMaChienDich(maChienDich);
        java.util.Set<String> oldStoreMas = oldStores.stream().map(CampaignStore::getMaStore).collect(java.util.stream.Collectors.toSet());
        
        java.util.Set<String> newStoreMas = new java.util.HashSet<>();
        if (storeMas != null) newStoreMas.addAll(storeMas);
        
        if (Boolean.TRUE.equals(campaign.getDocQuyen())) {
            try {
                validateExclusiveStores(campaign, newStoreMas);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/quanly/campaigns";
            }
        }
        
        java.util.Set<String> addedStores = new java.util.HashSet<>(newStoreMas);
        addedStores.removeAll(oldStoreMas);
        
        java.util.Set<String> removedStores = new java.util.HashSet<>(oldStoreMas);
        removedStores.removeAll(newStoreMas);

        campaignStoreRepository.deleteByMaChienDich(maChienDich);
        
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
            if (!addedStores.isEmpty()) desc.append(" thêm ").append(formatStoreList(addedStores));
            if (!removedStores.isEmpty()) {
                if (!addedStores.isEmpty()) desc.append(",");
                desc.append(" gỡ bỏ ").append(formatStoreList(removedStores));
            }
            logAudit("UPDATE", maChienDich, desc.toString());
        }
        
        return "redirect:/quanly/campaigns";
    }

    @GetMapping("/{campaignId}/rules")
    public String getCampaignRulesModal(@PathVariable Long campaignId, Model model) {
        Campaign campaign = getCampaignOrThrow(campaignId);
        String maChienDich = campaign.getMaChienDich();
        CampaignRule rule = campaignRuleRepository.findByMaChienDich(maChienDich).orElse(new CampaignRule());
        List<CampaignRulePayment> payments = campaignRulePaymentRepository.findByMaChienDich(maChienDich);
        List<CampaignRuleSku> skus = campaignRuleSkuRepository.findByMaChienDich(maChienDich);
        
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("basicRule", rule);
        model.addAttribute("paymentRules", payments);
        model.addAttribute("skuRules", skus);
        
        return "quanly/fragments/campaign-rules-fragment :: content";
    }

    @PostMapping("/{campaignId}/rules/save")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CHIENDICH_EDIT')")
    public String saveCampaignRules(@PathVariable Long campaignId, @ModelAttribute CampaignRuleForm form, RedirectAttributes redirectAttributes) {
        try {
            Campaign campaign = getCampaignOrThrow(campaignId);
            String maChienDich = campaign.getMaChienDich();
            
            CampaignRule oldBasicRule = campaignRuleRepository.findByMaChienDich(maChienDich).orElse(null);
            List<CampaignRulePayment> oldPayments = campaignRulePaymentRepository.findByMaChienDich(maChienDich);
            List<CampaignRuleSku> oldSkus = campaignRuleSkuRepository.findByMaChienDich(maChienDich);
            
            Double oldMinOrder = oldBasicRule != null ? oldBasicRule.getGiaTriDonHangToiThieu() : null;
            Double newMinOrder = form.getGiaTriDonHangToiThieu();
            boolean basicChanged = !java.util.Objects.equals(oldMinOrder, newMinOrder);
            
            java.util.Map<String, Integer> oldPaymentMap = oldPayments.stream().collect(Collectors.toMap(CampaignRulePayment::getPhuongThucThanhToan, CampaignRulePayment::getSoLuotThuong));
            java.util.Map<String, Integer> newPaymentMap = buildRuleMap(form.getPaymentMethods(), form.getPaymentTurns());
            boolean paymentChanged = !oldPaymentMap.equals(newPaymentMap);
            
            java.util.Map<String, Integer> oldSkuMap = oldSkus.stream().collect(Collectors.toMap(CampaignRuleSku::getMaSku, CampaignRuleSku::getSoLuotThuong));
            java.util.Map<String, Integer> newSkuMap = buildRuleMap(form.getSkuCodes(), form.getSkuTurns());
            boolean skuChanged = !oldSkuMap.equals(newSkuMap);

            campaignRuleRepository.deleteByMaChienDich(maChienDich);
            campaignRulePaymentRepository.deleteByMaChienDich(maChienDich);
            campaignRuleSkuRepository.deleteByMaChienDich(maChienDich);
            
            if (form.getGiaTriDonHangToiThieu() != null) {
                CampaignRule rule = new CampaignRule();
                rule.setMaChienDich(maChienDich);
                rule.setGiaTriDonHangToiThieu(form.getGiaTriDonHangToiThieu());
                campaignRuleRepository.save(rule);
            }
            
            for (java.util.Map.Entry<String, Integer> entry : newPaymentMap.entrySet()) {
                CampaignRulePayment payment = new CampaignRulePayment();
                payment.setMaChienDich(maChienDich);
                payment.setPhuongThucThanhToan(entry.getKey());
                payment.setSoLuotThuong(entry.getValue());
                campaignRulePaymentRepository.save(payment);
            }
            
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
                String ruleDesc = (newMinOrder == null && newPaymentMap.isEmpty() && newSkuMap.isEmpty()) 
                                ? "Xóa toàn bộ luật ưu đãi của chiến dịch" 
                                : "Chỉnh sửa luật ưu đãi: cập nhật " + String.join(", ", updatedRules) + " của chiến dịch";
                logAudit("UPDATE", maChienDich, ruleDesc);
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi lưu cấu hình luật: " + extractErrorMessage(e));
        }
        
        return "redirect:/quanly/campaigns";
    }

    @GetMapping("/{campaignId}/design")
    public String designMinigame(@PathVariable Long campaignId, Model model) {
        Campaign campaign = getCampaignOrThrow(campaignId);
        List<Prize> prizes = prizeRepository.findByMaChienDich(campaign.getMaChienDich());
        model.addAttribute("campaign", campaign);
        model.addAttribute("prizes", prizes);
        return "quanly/minigame-builder";
    }

    @PostMapping("/design/save")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ACT_CHIENDICH_EDIT')")
    public org.springframework.http.ResponseEntity<?> saveDesign(@RequestBody java.util.Map<String, String> payload) {
        try {
            Long campaignId = Long.parseLong(payload.get("campaignId"));
            Campaign campaign = getCampaignOrThrow(campaignId);
            
            campaign.setDuongDanSlug(payload.get("slug"));
            campaign.setCauhinhThemeJson(payload.get("configJson"));
            if (payload.get("bannerUrl") != null && !payload.get("bannerUrl").isEmpty()) {
                campaign.setHinhAnhUrl(payload.get("bannerUrl"));
            }
            campaignRepository.save(campaign);
            
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status", "success"));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Từ khóa đường dẫn (Slug) đã tồn tại. Vui lòng chọn từ khóa khác."));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", extractErrorMessage(e)));
        }
    }

    private String getUpdateDescription(Campaign existing, Campaign newValues) {
        List<String> changes = new java.util.ArrayList<>();
        if (!java.util.Objects.equals(existing.getMaChienDich(), newValues.getMaChienDich())) changes.add("mã chiến dịch");
        if (!java.util.Objects.equals(existing.getTenChienDich(), newValues.getTenChienDich())) changes.add("tên chiến dịch");
        if (!java.util.Objects.equals(existing.getNgayBatDau(), newValues.getNgayBatDau())) changes.add("ngày bắt đầu");
        if (!java.util.Objects.equals(existing.getNgayKetThuc(), newValues.getNgayKetThuc())) changes.add("ngày kết thúc");
        if (!java.util.Objects.equals(existing.getDuongDanSlug(), newValues.getDuongDanSlug())) changes.add("đường dẫn slug");
        if (newValues.getTrangThai() != null && !java.util.Objects.equals(existing.getTrangThai(), newValues.getTrangThai())) changes.add("trạng thái");
        if (!java.util.Objects.equals(existing.getMoTa(), newValues.getMoTa())) changes.add("mô tả");
        
        Boolean oldDocQuyen = existing.getDocQuyen() != null ? existing.getDocQuyen() : false;
        Boolean newDocQuyen = newValues.getDocQuyen() != null ? newValues.getDocQuyen() : false;
        if (!java.util.Objects.equals(oldDocQuyen, newDocQuyen)) changes.add("thiết lập độc quyền");
        if (!java.util.Objects.equals(existing.getHanTokenNgay(), newValues.getHanTokenNgay())) changes.add("hạn token");
        if (!java.util.Objects.equals(existing.getSoNgayHienThiThem(), newValues.getSoNgayHienThiThem())) changes.add("số ngày hiển thị thêm");
        
        return changes.isEmpty() ? "Chỉnh sửa thông tin chiến dịch" : "Chỉnh sửa " + String.join(", ", changes) + " của chiến dịch";
    }

    private String formatStoreList(java.util.Set<String> stores) {
        List<String> list = new java.util.ArrayList<>(stores);
        return list.size() <= 5 ? String.join(", ", list) : String.join(", ", list.subList(0, 5)) + " và " + (list.size() - 5) + " cửa hàng khác";
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
                c.getDisplayStatus()
            }).collect(java.util.stream.Collectors.toList());
            
            com.bitis.luckydraw.util.ExcelExportUtil.exportDataToExcel(response, "DanhSachChienDich", headers, data);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
