package com.bitis.luckydraw.service;

import com.bitis.luckydraw.dto.InvoiceRequestDTO;
import com.bitis.luckydraw.model.*;
import com.bitis.luckydraw.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TurnManagementService {

    private final CustomerRepository customerRepo;
    private final InvoiceRepository invoiceRepo;
    private final CampaignStoreRepository campaignStoreRepo;
    private final CampaignRepository campaignRepo;
    private final DeltaRuleEngine ruleEngine;
    private final CustomerTurnRepository customerTurnRepo;
    private final TurnTransactionRepository turnTransactionRepo;
    private final GameAccessTokenRepository tokenRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TurnManagementService(CustomerRepository customerRepo, InvoiceRepository invoiceRepo, CampaignStoreRepository campaignStoreRepo, CampaignRepository campaignRepo, DeltaRuleEngine ruleEngine, CustomerTurnRepository customerTurnRepo, TurnTransactionRepository turnTransactionRepo, GameAccessTokenRepository tokenRepo) {
        this.customerRepo = customerRepo;
        this.invoiceRepo = invoiceRepo;
        this.campaignStoreRepo = campaignStoreRepo;
        this.campaignRepo = campaignRepo;
        this.ruleEngine = ruleEngine;
        this.customerTurnRepo = customerTurnRepo;
        this.turnTransactionRepo = turnTransactionRepo;
        this.tokenRepo = tokenRepo;
    }

    @Transactional
    public GameAccessToken processInvoice(InvoiceRequestDTO request) throws Exception {
        // Không tạo Customer rác, chỉ dùng SĐT để làm mã KH tham chiếu cho hóa đơn
        String maKhachHangThamChieu = "CUS-" + request.getCustomerPhone();

        // 2. Tính Delta Amount
        Double deltaAmount = request.getTotalAmount();
        if (request.getOriginalInvoiceNumber() != null && !request.getOriginalInvoiceNumber().isEmpty()) {
            if (invoiceRepo.existsByMaHoaDonGoc(request.getOriginalInvoiceNumber())) {
                throw new Exception("Mã hóa đơn gốc đã được sử dụng để đổi hàng trước đó!");
            }
            Optional<Invoice> originalOpt = invoiceRepo.findByMaHoaDon(request.getOriginalInvoiceNumber());
            if (originalOpt.isPresent()) {
                deltaAmount = request.getTotalAmount() - originalOpt.get().getTongTien();
            }
        }

        // Nếu Delta <= 0, không cấp lượt nhưng vẫn ghi nhận hóa đơn
        if (deltaAmount <= 0) {
            saveInvoice(request, maKhachHangThamChieu, true); // true = Đã xử lý (không được cộng lượt nữa)
            return null; // Không trả về token
        }

        // 3. Lưu Invoice mới với trạng thái daXuLy = false (chờ khách hàng claim)
        Invoice invoice = saveInvoice(request, maKhachHangThamChieu, false);

        // 4. Lấy danh sách Campaign đang chạy tại Store này
        List<CampaignStore> campaigns = campaignStoreRepo.findByMaStore(request.getMaStore());
        
        int totalTurnsGrantedAcrossCampaigns = 0;

        for (CampaignStore cs : campaigns) {
            String maChienDich = cs.getMaChienDich();
            
            // ponytail: chỉ cộng lượt cho chiến dịch đang chạy tại thời điểm xuất hóa đơn
            Campaign c = campaignRepo.findByMaChienDich(maChienDich).orElse(null);
            if (c == null || c.getTrangThai() == null || c.getTrangThai() != 1) continue;
            if (c.getNgayBatDau() != null && c.getNgayBatDau().isAfter(invoice.getNgayTao())) continue;
            if (c.getNgayKetThuc() != null && c.getNgayKetThuc().isBefore(invoice.getNgayTao())) continue;

            int turns = ruleEngine.calculateTurns(maChienDich, deltaAmount, request.getPaymentMethod(), request.getSkuList());

            if (turns > 0) {
                // CHỈ TÍNH LƯỢT CHỨ KHÔNG CỘNG LƯỢT TRỰC TIẾP VÀO DB
                totalTurnsGrantedAcrossCampaigns += turns;
            }
        }

        // 5. Nếu được cấp ít nhất 1 lượt, sinh GameAccessToken
        if (totalTurnsGrantedAcrossCampaigns > 0) {
            int maxHanTokenNgay = 0;
            LocalDateTime absoluteExpiration = null;
            LocalDateTime maxNgayKetThuc = null;
            
            for (CampaignStore cs : campaigns) {
                Campaign c = campaignRepo.findByMaChienDich(cs.getMaChienDich()).orElse(null);
                if (c != null) {
                    if (c.getNgayKetThuc() != null) {
                        if (maxNgayKetThuc == null || c.getNgayKetThuc().isAfter(maxNgayKetThuc)) {
                            maxNgayKetThuc = c.getNgayKetThuc();
                        }
                    }
                    if (c.getHanTokenNgay() != null) {
                        if (c.getHanTokenNgay() > 0) {
                            maxHanTokenNgay = Math.max(maxHanTokenNgay, c.getHanTokenNgay());
                        } else if (c.getHanTokenNgay() == 0 && c.getNgayKetThuc() != null) {
                            if (absoluteExpiration == null || c.getNgayKetThuc().isAfter(absoluteExpiration)) {
                                absoluteExpiration = c.getNgayKetThuc();
                            }
                        }
                    }
                }
            }
            
            LocalDateTime finalExpiration = null;
            if (maxHanTokenNgay > 0) {
                finalExpiration = LocalDateTime.now().plusDays(maxHanTokenNgay);
            }
            if (absoluteExpiration != null) {
                if (finalExpiration == null || absoluteExpiration.isAfter(finalExpiration)) {
                    finalExpiration = absoluteExpiration;
                }
            }
            if (finalExpiration == null) {
                finalExpiration = LocalDateTime.now().plusDays(30);
            }

            // ponytail: Chặn 2 điều kiện - Không cho phép hạn Token vượt quá Ngày Kết Thúc chiến dịch
            if (maxNgayKetThuc != null && finalExpiration.isAfter(maxNgayKetThuc)) {
                finalExpiration = maxNgayKetThuc;
            }

            GameAccessToken token = new GameAccessToken();
            token.setToken(java.util.UUID.randomUUID().toString());
            token.setMaHoaDon(invoice.getMaHoaDon());
            token.setSoLuongLuotThuong(totalTurnsGrantedAcrossCampaigns);
            token.setDaSuDung(false);
            token.setMaKhachHangKichHoat(null); // ponytail: chưa ai claim, set khi quét QR
            token.setHetHanLuc(finalExpiration);
            return tokenRepo.save(token);
        }

        return null;
    }

    private Invoice saveInvoice(InvoiceRequestDTO request, String maKhachHang, boolean daXuLy) throws Exception {
        Invoice invoice = new Invoice();
        invoice.setMaHoaDon(request.getInvoiceNumber());
        invoice.setMaHoaDonGoc(request.getOriginalInvoiceNumber());
        invoice.setMaStore(request.getMaStore());
        invoice.setMaKhachHang(maKhachHang);
        invoice.setTongTien(request.getTotalAmount());
        invoice.setPhuongThucTt(request.getPaymentMethod());
        invoice.setSanPhamJson(objectMapper.writeValueAsString(request.getSkuList()));
        invoice.setDaXuLy(daXuLy);
        return invoiceRepo.save(invoice);
    }

    @Transactional
    public java.util.List<String> claimInvoice(String maHoaDon, String maKhachHang) throws Exception {
        Optional<Invoice> optInvoice = invoiceRepo.findByMaHoaDon(maHoaDon);
        if (optInvoice.isEmpty()) {
            throw new Exception("Không tìm thấy hóa đơn này trên hệ thống.");
        }
        
        Invoice invoice = optInvoice.get();
        if (Boolean.TRUE.equals(invoice.getDaXuLy())) {
            throw new Exception("Hóa đơn này đã được nhận lượt quay trước đó.");
        }

        // Tính toán lượt quay
        Double deltaAmount = invoice.getTongTien();
        if (invoice.getMaHoaDonGoc() != null && !invoice.getMaHoaDonGoc().isEmpty()) {
            Optional<Invoice> originalOpt = invoiceRepo.findByMaHoaDon(invoice.getMaHoaDonGoc());
            if (originalOpt.isPresent()) {
                deltaAmount = invoice.getTongTien() - originalOpt.get().getTongTien();
            }
        }

        java.util.List<String> awardedCampaigns = new java.util.ArrayList<>();
        int totalTurnsGranted = 0;

        if (deltaAmount > 0) {
            List<CampaignStore> campaigns = campaignStoreRepo.findByMaStore(invoice.getMaStore());
            
            // Reconstruct SKU list from JSON
            List<InvoiceRequestDTO.SkuItem> skuList = new java.util.ArrayList<>();
            if (invoice.getSanPhamJson() != null && !invoice.getSanPhamJson().isEmpty()) {
                try {
                    skuList = objectMapper.readValue(invoice.getSanPhamJson(), objectMapper.getTypeFactory().constructCollectionType(List.class, InvoiceRequestDTO.SkuItem.class));
                } catch (Exception ignored) {}
            }

            for (CampaignStore cs : campaigns) {
                String maChienDich = cs.getMaChienDich();
                
                // ponytail: chỉ cộng lượt cho chiến dịch đang chạy tại thời điểm xuất hóa đơn
                Campaign c = campaignRepo.findByMaChienDich(maChienDich).orElse(null);
                if (c == null || c.getTrangThai() == null || c.getTrangThai() != 1) continue;
                if (c.getNgayBatDau() != null && c.getNgayBatDau().isAfter(invoice.getNgayTao())) continue;
                if (c.getNgayKetThuc() != null && c.getNgayKetThuc().isBefore(invoice.getNgayTao())) continue;

                int turns = ruleEngine.calculateTurns(maChienDich, deltaAmount, invoice.getPhuongThucTt(), skuList);

                if (turns > 0) {
                    customerTurnRepo.addCustomerTurnsSafe(maKhachHang, maChienDich, turns, maHoaDon);
                    awardedCampaigns.add(maChienDich);
                    totalTurnsGranted += turns;
                }
            }
        }

        // Đánh dấu đã xử lý
        invoice.setDaXuLy(true);
        invoiceRepo.save(invoice);
        
        return awardedCampaigns; // Trả về danh sách các campaign đã được cộng lượt
    }

    @Transactional
    public boolean useGameAccessToken(String qrCode, String maKhachHang) throws Exception {
        Optional<GameAccessToken> optToken = tokenRepo.findByToken(qrCode);
        if (optToken.isPresent()) {
            GameAccessToken token = optToken.get();
            if (Boolean.TRUE.equals(token.getDaSuDung())) {
                throw new Exception("Mã QR (Hóa đơn) này đã được sử dụng để vào game.");
            }

            // --- PONYTAIL APPROACH (MỚI): GỌI CLAIM INVOICE ĐỂ CẤP PHÁT LƯỢT TRỰC TIẾP ---
            claimInvoice(token.getMaHoaDon(), maKhachHang);
            
            token.setDaSuDung(true);
            token.setNgaySuDung(LocalDateTime.now());
            token.setMaKhachHangKichHoat(maKhachHang);
            tokenRepo.save(token);
            return true; // Đã xử lý token thành công
        }
        return false; // Token chưa tồn tại
    }
}
