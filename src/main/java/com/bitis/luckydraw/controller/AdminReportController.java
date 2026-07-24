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
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/quanly/report")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('QL_BAOCAODOANHTHU')")
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
                                @RequestParam(required = false) String phanLoai,
                                Model model) {
        
        List<Campaign> campaigns = new ArrayList<>();
        List<Store> stores = new ArrayList<>();
        List<Map<String, Object>> detailedInvoices = new ArrayList<>();
        Map<String, Map<String, Object>> summaryMap = new HashMap<>();
        
        buildReportData(maChienDich, maStore, phanLoai, campaigns, stores, detailedInvoices, summaryMap);

        Map<String, Map<String, Object>> summaryGroups = new HashMap<>();
        for (Map<String, Object> item : summaryMap.values()) {
            String cId = (String) item.get("maChienDich");
            Map<String, Object> group = summaryGroups.computeIfAbsent(cId, k -> {
                Map<String, Object> g = new HashMap<>();
                g.put("tenChienDich", item.get("tenChienDich"));
                g.put("maChienDich", item.get("maChienDich"));
                g.put("stores", new ArrayList<Map<String, Object>>());
                g.put("soHoaDon", 0);
                g.put("soToken", 0);
                g.put("soLuotCap", 0);
                g.put("doanhThuDon", 0.0);
                g.put("doanhThuDa", 0.0);
                g.put("doanhThuTong", 0.0);
                return g;
            });
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> storeList = (List<Map<String, Object>>) group.get("stores");
            storeList.add(item);
            
            group.put("soHoaDon", (Integer) group.get("soHoaDon") + (Integer) item.get("soHoaDon"));
            group.put("soToken", (Integer) group.get("soToken") + (Integer) item.get("soToken"));
            group.put("soLuotCap", (Integer) group.get("soLuotCap") + (Integer) item.get("soLuotCap"));
            group.put("doanhThuDon", (Double) group.get("doanhThuDon") + (Double) item.get("doanhThuDon"));
            group.put("doanhThuDa", (Double) group.get("doanhThuDa") + (Double) item.get("doanhThuDa"));
            group.put("doanhThuTong", (Double) group.get("doanhThuTong") + (Double) item.get("doanhThuTong"));
        }

        model.addAttribute("campaigns", campaigns);
        model.addAttribute("stores", stores);
        model.addAttribute("summary", new ArrayList<>(summaryGroups.values()));
        model.addAttribute("details", detailedInvoices);
        model.addAttribute("selectedCampaign", maChienDich);
        model.addAttribute("selectedStore", maStore);
        model.addAttribute("selectedPhanLoai", phanLoai);
        
        return "quanly/report-revenue";
    }

    @GetMapping("/revenue/export-excel")
    public void exportRevenueExcel(@RequestParam(required = false) String maChienDich, 
                                @RequestParam(required = false) String maStore, 
                                @RequestParam(required = false) String phanLoai,
                                HttpServletResponse response) throws IOException {
        
        Map<String, Map<String, Object>> summaryMap = new HashMap<>();
        buildReportData(maChienDich, maStore, phanLoai, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), summaryMap);
        
        String[] headers = {"Chiến dịch", "Cửa hàng", "Số Hóa Đơn", "Số Token", "Lượt Cấp", "DT Đơn Chiến Dịch", "DT Đa Chiến Dịch", "Tổng Doanh Thu"};
        List<String[]> data = new ArrayList<>();
        for (Map<String, Object> item : summaryMap.values()) {
            data.add(new String[] {
                (String) item.get("tenChienDich"),
                (String) item.get("tenStore"),
                String.valueOf(item.get("soHoaDon")),
                String.valueOf(item.get("soToken")),
                String.valueOf(item.get("soLuotCap")),
                String.format(Locale.US, "%.0f", item.get("doanhThuDon")),
                String.format(Locale.US, "%.0f", item.get("doanhThuDa")),
                String.format(Locale.US, "%.0f", item.get("doanhThuTong"))
            });
        }
        com.bitis.luckydraw.util.ExcelExportUtil.exportDataToExcel(response, "BaoCaoDoanhThuTongHop", headers, data);
    }
    
    private void buildReportData(String maChienDich, String maStore, String phanLoai, 
                                 List<Campaign> outCampaigns, List<Store> outStores, 
                                 List<Map<String, Object>> outDetailedInvoices, 
                                 Map<String, Map<String, Object>> outSummaryMap) {
        List<Campaign> campaigns = campaignRepository.findAll();
        List<Store> stores = storeRepository.findAll();
        
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        java.util.List<String> allowedStores = (principal instanceof com.bitis.luckydraw.security.CustomUserDetails ud) ? ud.getEffectiveStores() : null;
        if (allowedStores != null) {
            stores.removeIf(s -> !allowedStores.contains(s.getMaStore()));
        }
        outCampaigns.addAll(campaigns);
        outStores.addAll(stores);
        
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
        
        if (allowedStores != null) {
            allMappings.removeIf(m -> !allowedStores.contains(m.getMaStore()));
        }
        
        if (!allMappings.isEmpty()) {
            List<String> invoiceCodes = allMappings.stream().map(InvoiceCampaign::getMaHoaDon).distinct().collect(Collectors.toList());
            List<InvoiceCampaign> allInvoicesMappings = invoiceCampaignRepository.findByMaHoaDonIn(invoiceCodes);
            Map<String, List<InvoiceCampaign>> mappingsByInvoice = allInvoicesMappings.stream().collect(Collectors.groupingBy(InvoiceCampaign::getMaHoaDon));
            
            List<GameAccessToken> tokens = new ArrayList<>();
            for(String invCode : invoiceCodes) {
                gameAccessTokenRepository.findByMaHoaDon(invCode).ifPresent(tokens::add);
            }
            Map<String, GameAccessToken> tokenMap = tokens.stream().collect(Collectors.toMap(GameAccessToken::getMaHoaDon, t -> t, (a,b)->a));

            for (InvoiceCampaign ic : allMappings) {
                List<InvoiceCampaign> appliedTo = mappingsByInvoice.get(ic.getMaHoaDon());
                boolean isMulti = appliedTo.size() > 1;
                
                boolean matchFilter = true;
                if (phanLoai != null && !phanLoai.isEmpty()) {
                    if ("DON".equals(phanLoai) && isMulti) matchFilter = false;
                    if ("DA".equals(phanLoai) && !isMulti) matchFilter = false;
                }
                if (!matchFilter) continue;
                
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
                
                Map<String, Object> detail = new HashMap<>();
                detail.put("maHoaDon", ic.getMaHoaDon());
                detail.put("token", tokenString);
                detail.put("maChienDich", ic.getMaChienDich());
                detail.put("tenChienDich", campaigns.stream().filter(c -> c.getMaChienDich().equals(ic.getMaChienDich())).findFirst().map(Campaign::getTenChienDich).orElse(ic.getMaChienDich()));
                detail.put("maStore", ic.getMaStore());
                detail.put("tenStore", stores.stream().filter(s -> s.getMaStore().equals(ic.getMaStore())).findFirst().map(Store::getTenCuaHang).orElse(ic.getMaStore()));
                detail.put("doanhThu", String.format(Locale.US, "%.0f", ic.getTongTien()));
                detail.put("soLuotCap", String.valueOf(ic.getSoLuotCap()));
                detail.put("trangThai", isActivated ? "Đã kích hoạt" : "Chưa kích hoạt");
                detail.put("chuThich", note);
                detail.put("isMulti", isMulti);
                
                outDetailedInvoices.add(detail);
                
                if (outSummaryMap != null) {
                    String key = ic.getMaChienDich() + "_" + ic.getMaStore();
                    Map<String, Object> sumItem = outSummaryMap.computeIfAbsent(key, k -> {
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
        }
    }
}
