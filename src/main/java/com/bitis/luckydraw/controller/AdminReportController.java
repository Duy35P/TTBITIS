package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.model.Campaign;
import com.bitis.luckydraw.model.GameAccessToken;
import com.bitis.luckydraw.model.InvoiceCampaign;
import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.repository.CampaignRepository;
import com.bitis.luckydraw.repository.GameAccessTokenRepository;
import com.bitis.luckydraw.repository.InvoiceCampaignRepository;
import com.bitis.luckydraw.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.annotation.PostConstruct;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/quanly/report")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    @Autowired
    private InvoiceCampaignRepository invoiceCampaignRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private GameAccessTokenRepository gameAccessTokenRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void autoFixDatabase() {
        try {
            jdbcTemplate.execute("ALTER TABLE invoice_campaign ADD so_luot_cap INT NOT NULL DEFAULT 0");
            System.out.println("Đã tự động thêm cột so_luot_cap vào bảng invoice_campaign.");
        } catch (Exception e) {
            // Bỏ qua nếu cột đã tồn tại
        }
    }

    @GetMapping("/revenue")
    public String revenueReport(@RequestParam(required = false) String maChienDich, 
                                @RequestParam(required = false) String maStore, 
                                Model model) {
        
        List<Campaign> campaigns = campaignRepository.findAll();
        List<Store> stores = storeRepository.findAll();
        
        List<InvoiceCampaign> allMappings;
        if (maChienDich != null && !maChienDich.isEmpty() && maStore != null && !maStore.isEmpty()) {
            allMappings = invoiceCampaignRepository.findByMaChienDichAndMaStore(maChienDich, maStore);
        } else if (maChienDich != null && !maChienDich.isEmpty()) {
            allMappings = invoiceCampaignRepository.findByMaChienDich(maChienDich);
        } else if (maStore != null && !maStore.isEmpty()) {
            allMappings = invoiceCampaignRepository.findByMaStore(maStore);
        } else {
            allMappings = invoiceCampaignRepository.findAll();
        }

        // Prepare Summary
        Map<String, Map<String, Object>> summaryMap = new HashMap<>();
        // Group by ChienDich + "_" + Store
        
        // Prepare Detailed Invoices
        List<Map<String, Object>> detailedInvoices = new ArrayList<>();
        
        if (!allMappings.isEmpty()) {
            // To figure out if an invoice is multi-campaign, we need ALL mappings for the invoices found
            List<String> invoiceCodes = allMappings.stream().map(InvoiceCampaign::getMaHoaDon).distinct().collect(Collectors.toList());
            List<InvoiceCampaign> allInvoicesMappings = invoiceCampaignRepository.findByMaHoaDonIn(invoiceCodes);
            
            // Map Invoice -> List of campaigns it applied to
            Map<String, List<InvoiceCampaign>> mappingsByInvoice = allInvoicesMappings.stream()
                .collect(Collectors.groupingBy(InvoiceCampaign::getMaHoaDon));
                
            // Fetch Tokens for these invoices to know if they are activated
            List<GameAccessToken> tokens = new ArrayList<>();
            for(String invCode : invoiceCodes) {
                gameAccessTokenRepository.findByMaHoaDon(invCode).ifPresent(tokens::add);
            }
            Map<String, GameAccessToken> tokenMap = tokens.stream()
                .collect(Collectors.toMap(GameAccessToken::getMaHoaDon, t -> t, (a,b)->a));

            for (InvoiceCampaign ic : allMappings) {
                // Determine Multi-campaign status
                List<InvoiceCampaign> appliedTo = mappingsByInvoice.get(ic.getMaHoaDon());
                boolean isMulti = appliedTo.size() > 1;
                
                String note = "Đơn chiến dịch";
                if (isMulti) {
                    String otherCampaigns = appliedTo.stream()
                        .filter(m -> !m.getMaChienDich().equals(ic.getMaChienDich()))
                        .map(m -> campaigns.stream().filter(c -> c.getMaChienDich().equals(m.getMaChienDich())).findFirst().map(Campaign::getTenChienDich).orElse(m.getMaChienDich()))
                        .collect(Collectors.joining(", "));
                    note = "Đa chiến dịch (Cùng với: " + otherCampaigns + ")";
                }
                
                GameAccessToken tokenObj = tokenMap.get(ic.getMaHoaDon());
                boolean isActivated = tokenObj != null && Boolean.TRUE.equals(tokenObj.getDaSuDung());
                String tokenString = tokenObj != null ? tokenObj.getToken() : "";
                
                // Add to details
                Map<String, Object> detail = new HashMap<>();
                detail.put("maHoaDon", ic.getMaHoaDon());
                detail.put("token", tokenString);
                detail.put("maChienDich", ic.getMaChienDich());
                detail.put("tenChienDich", campaigns.stream().filter(c -> c.getMaChienDich().equals(ic.getMaChienDich())).findFirst().map(Campaign::getTenChienDich).orElse(ic.getMaChienDich()));
                detail.put("maStore", ic.getMaStore());
                detail.put("tenStore", stores.stream().filter(s -> s.getMaStore().equals(ic.getMaStore())).findFirst().map(Store::getTenCuaHang).orElse(ic.getMaStore()));
                detail.put("doanhThu", ic.getTongTien());
                detail.put("soLuotCap", ic.getSoLuotCap());
                detail.put("trangThai", isActivated ? "Đã kích hoạt" : "Chưa kích hoạt");
                detail.put("chuThich", note);
                detail.put("isMulti", isMulti);
                detailedInvoices.add(detail);
                
                // Add to summary
                String key = ic.getMaChienDich() + "_" + ic.getMaStore();
                Map<String, Object> sumItem = summaryMap.computeIfAbsent(key, k -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maChienDich", ic.getMaChienDich());
                    map.put("tenChienDich", detail.get("tenChienDich"));
                    map.put("maStore", ic.getMaStore());
                    map.put("tenStore", detail.get("tenStore"));
                    map.put("soHoaDon", 0);
                    map.put("soToken", 0);
                    map.put("doanhThuTong", 0.0);
                    map.put("doanhThuDon", 0.0);
                    map.put("doanhThuDa", 0.0);
                    map.put("soLuotCap", 0);
                    return map;
                });
                
                sumItem.put("soHoaDon", (Integer) sumItem.get("soHoaDon") + 1);
                if (tokenObj != null) {
                    sumItem.put("soToken", (Integer) sumItem.get("soToken") + 1);
                }
                sumItem.put("doanhThuTong", (Double) sumItem.get("doanhThuTong") + ic.getTongTien());
                sumItem.put("soLuotCap", (Integer) sumItem.get("soLuotCap") + ic.getSoLuotCap());
                if (isMulti) {
                    sumItem.put("doanhThuDa", (Double) sumItem.get("doanhThuDa") + ic.getTongTien());
                } else {
                    sumItem.put("doanhThuDon", (Double) sumItem.get("doanhThuDon") + ic.getTongTien());
                }
            }
        }

        model.addAttribute("campaigns", campaigns);
        model.addAttribute("stores", stores);
        model.addAttribute("summary", summaryMap.values());
        model.addAttribute("details", detailedInvoices);
        model.addAttribute("selectedCampaign", maChienDich);
        model.addAttribute("selectedStore", maStore);
        
        return "quanly/report-revenue";
    }
}
