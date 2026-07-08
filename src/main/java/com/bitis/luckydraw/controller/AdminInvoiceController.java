package com.bitis.luckydraw.controller;

import com.bitis.luckydraw.dto.InvoiceListDto;
import com.bitis.luckydraw.model.Campaign;
import com.bitis.luckydraw.model.Customer;
import com.bitis.luckydraw.model.GameAccessToken;
import com.bitis.luckydraw.model.Invoice;
import com.bitis.luckydraw.model.Store;
import com.bitis.luckydraw.model.TurnTransaction;
import com.bitis.luckydraw.repository.CampaignRepository;
import com.bitis.luckydraw.repository.CustomerRepository;
import com.bitis.luckydraw.repository.GameAccessTokenRepository;
import com.bitis.luckydraw.repository.InvoiceRepository;
import com.bitis.luckydraw.repository.StoreRepository;
import com.bitis.luckydraw.repository.TurnTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/invoices")
public class AdminInvoiceController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TurnTransactionRepository turnTransactionRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private GameAccessTokenRepository gameAccessTokenRepository;

    @GetMapping
    public String index(Model model,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "status", required = false) String status) {

        List<Invoice> rawInvoices = invoiceRepository.findAll();
        
        // Caching for Stores and Campaigns
        Map<String, String> storeNames = storeRepository.findAll().stream()
                .collect(Collectors.toMap(Store::getMaStore, Store::getTenCuaHang));
                
        // LÀM ĐƠN GIẢN: Filter based on User Details
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        List<Invoice> invoices = rawInvoices;
        
        if (auth != null && auth.getPrincipal() instanceof com.bitis.luckydraw.security.CustomUserDetails) {
            com.bitis.luckydraw.security.CustomUserDetails userDetails = (com.bitis.luckydraw.security.CustomUserDetails) auth.getPrincipal();
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                if (userDetails.getAssignedStores() != null && !userDetails.getAssignedStores().isEmpty()) {
                    invoices = rawInvoices.stream()
                        .filter(inv -> userDetails.getAssignedStores().contains(inv.getMaStore()))
                        .collect(Collectors.toList());
                } else if (userDetails.getMaStore() != null) {
                    invoices = rawInvoices.stream()
                        .filter(inv -> userDetails.getMaStore().equals(inv.getMaStore()))
                        .collect(Collectors.toList());
                }
            }
        }
        
        Map<String, String> campaignNames = campaignRepository.findAll().stream()
                .collect(Collectors.toMap(Campaign::getMaChienDich, Campaign::getTenChienDich));

        List<InvoiceListDto> dtos = new ArrayList<>();

        for (Invoice inv : invoices) {
            String customerPhone = "N/A";
            if (inv.getMaKhachHang() != null && !inv.getMaKhachHang().isEmpty()) {
                Customer c = customerRepository.findByMaKhachHang(inv.getMaKhachHang()).orElse(null);
                if (c != null) {
                    customerPhone = c.getPhone();
                } else if (inv.getMaKhachHang().startsWith("CUS-")) {
                    customerPhone = inv.getMaKhachHang().replace("CUS-", "");
                }
            }

            // Apply filters before doing complex turn fetching
            boolean matchKeyword = true;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.toLowerCase();
                matchKeyword = (inv.getMaHoaDon() != null && inv.getMaHoaDon().toLowerCase().contains(kw)) ||
                               customerPhone.toLowerCase().contains(kw);
            }

            boolean matchStatus = true;
            List<TurnTransaction> turns = null;
            
            if (status != null && !status.isEmpty() && !status.equals("all")) {
                if (status.equals("0")) {
                    matchStatus = Boolean.FALSE.equals(inv.getDaXuLy());
                } else if (status.equals("1")) {
                    matchStatus = Boolean.TRUE.equals(inv.getDaXuLy());
                    if (matchStatus) {
                        turns = turnTransactionRepository.findByNguonThamChieu(inv.getMaHoaDon());
                        matchStatus = turns.stream().mapToLong(TurnTransaction::getSoLuong).sum() > 0;
                    }
                } else if (status.equals("2")) {
                    matchStatus = Boolean.TRUE.equals(inv.getDaXuLy());
                    if (matchStatus) {
                        turns = turnTransactionRepository.findByNguonThamChieu(inv.getMaHoaDon());
                        matchStatus = turns.stream().mapToLong(TurnTransaction::getSoLuong).sum() == 0;
                    }
                }
            }

            if (matchKeyword && matchStatus) {
                InvoiceListDto dto = new InvoiceListDto();
                dto.setMaHoaDon(inv.getMaHoaDon());
                dto.setNgayTao(inv.getNgayTao());
                dto.setTongTien(inv.getTongTien());
                dto.setPhuongThucTt(inv.getPhuongThucTt());
                dto.setSanPhamJson(inv.getSanPhamJson());
                dto.setDaXuLy(inv.getDaXuLy());
                dto.setTenCuaHang(storeNames.getOrDefault(inv.getMaStore(), inv.getMaStore()));
                dto.setKhachHangSdt(customerPhone);
                
                // Get turns
                if (turns == null) {
                    turns = turnTransactionRepository.findByNguonThamChieu(inv.getMaHoaDon());
                }
                Map<String, Integer> campaignTurns = turns.stream()
                        .filter(t -> t.getSoLuong() > 0)
                        .collect(Collectors.groupingBy(TurnTransaction::getMaChienDich, Collectors.summingInt(TurnTransaction::getSoLuong)));
                
                List<String> chiTietCapLuot = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : campaignTurns.entrySet()) {
                    String campName = campaignNames.getOrDefault(entry.getKey(), entry.getKey());
                    chiTietCapLuot.add(campName + " (" + entry.getValue() + " lượt)");
                }
                dto.setChiTietCapLuot(chiTietCapLuot);
                gameAccessTokenRepository.findByMaHoaDon(inv.getMaHoaDon())
                        .ifPresent(gat -> {
                            dto.setGameAccessToken(gat.getToken());
                            dto.setTokenDaSuDung(gat.getDaSuDung());
                            dto.setTokenHetHan(gat.getHetHanLuc());
                            dto.setTokenNgaySuDung(gat.getNgaySuDung());
                        });
                
                dtos.add(dto);
            }
        }

        model.addAttribute("invoices", dtos);
        model.addAttribute("selectedKeyword", keyword != null ? keyword : "");
        model.addAttribute("selectedStatus", status != null ? status : "all");

        return "admin/invoice-list";
    }

    @GetMapping("/export-excel")
    public void exportExcel(Model model,
                            @RequestParam(name = "keyword", required = false) String keyword,
                            @RequestParam(name = "status", required = false) String status,
                            jakarta.servlet.http.HttpServletResponse response) {
        try {
            // Reuse index logic to get filtered list
            this.index(model, keyword, status);
            List<InvoiceListDto> dtos = (List<InvoiceListDto>) model.getAttribute("invoices");
            
            String[] headers = {"Mã Hóa Đơn", "Cửa Hàng", "SĐT Khách Hàng", "Ngày Tạo", "Tổng Tiền", "Đã Xử Lý Cấp Lượt", "Chi Tiết Cấp Lượt", "Mã Vào Game"};
            List<String[]> data = dtos.stream().map(dto -> new String[]{
                dto.getMaHoaDon(),
                dto.getTenCuaHang(),
                dto.getKhachHangSdt(),
                dto.getNgayTao() != null ? dto.getNgayTao().toString() : "",
                dto.getTongTien() != null ? dto.getTongTien().toString() : "0",
                dto.getDaXuLy() != null && dto.getDaXuLy() ? "Đã xử lý" : "Chưa xử lý",
                String.join(", ", dto.getChiTietCapLuot()),
                dto.getGameAccessToken() != null ? dto.getGameAccessToken() : ""
            }).collect(Collectors.toList());
            
            com.bitis.luckydraw.util.ExcelExportUtil.exportDataToExcel(response, "DanhSachHoaDon", headers, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
