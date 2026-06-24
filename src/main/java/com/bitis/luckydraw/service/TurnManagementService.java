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
    private final DeltaRuleEngine ruleEngine;
    private final CustomerTurnRepository customerTurnRepo;
    private final TurnTransactionRepository turnTransactionRepo;
    private final GameAccessTokenRepository tokenRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TurnManagementService(CustomerRepository customerRepo, InvoiceRepository invoiceRepo, CampaignStoreRepository campaignStoreRepo, DeltaRuleEngine ruleEngine, CustomerTurnRepository customerTurnRepo, TurnTransactionRepository turnTransactionRepo, GameAccessTokenRepository tokenRepo) {
        this.customerRepo = customerRepo;
        this.invoiceRepo = invoiceRepo;
        this.campaignStoreRepo = campaignStoreRepo;
        this.ruleEngine = ruleEngine;
        this.customerTurnRepo = customerTurnRepo;
        this.turnTransactionRepo = turnTransactionRepo;
        this.tokenRepo = tokenRepo;
    }

    @Transactional
    public GameAccessToken processInvoice(InvoiceRequestDTO request) throws Exception {
        // 1. Tìm hoặc tạo Customer
        Customer customer = customerRepo.findByPhone(request.getCustomerPhone())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setMaKhachHang("CUS-" + request.getCustomerPhone());
                    newCustomer.setPhone(request.getCustomerPhone());
                    newCustomer.setTrangThai(1);
                    return customerRepo.save(newCustomer);
                });

        // 2. Tính Delta Amount
        Double deltaAmount = request.getTotalAmount();
        if (request.getOriginalInvoiceNumber() != null && !request.getOriginalInvoiceNumber().isEmpty()) {
            Optional<Invoice> originalOpt = invoiceRepo.findByMaHoaDon(request.getOriginalInvoiceNumber());
            if (originalOpt.isPresent()) {
                deltaAmount = request.getTotalAmount() - originalOpt.get().getTongTien();
            }
        }

        // Nếu Delta <= 0, không cấp lượt nhưng vẫn ghi nhận hóa đơn
        if (deltaAmount <= 0) {
            saveInvoice(request, customer.getMaKhachHang());
            return null; // Không trả về token
        }

        // 3. Lưu Invoice mới
        Invoice invoice = saveInvoice(request, customer.getMaKhachHang());

        // 4. Lấy danh sách Campaign đang chạy tại Store này
        List<CampaignStore> campaigns = campaignStoreRepo.findByMaStore(request.getMaStore());
        
        int totalTurnsGrantedAcrossCampaigns = 0;

        for (CampaignStore cs : campaigns) {
            String maChienDich = cs.getMaChienDich();
            int turns = ruleEngine.calculateTurns(maChienDich, deltaAmount, request.getPaymentMethod(), request.getSkuList());

            if (turns > 0) {
                // Gọi Stored Procedure (SP tự quản lý Upsert Lock và Ghi Transaction)
                customerTurnRepo.addCustomerTurnsSafe(
                        customer.getMaKhachHang(),
                        maChienDich,
                        turns,
                        "INVOICE:" + invoice.getMaHoaDon()
                );

                totalTurnsGrantedAcrossCampaigns += turns;
            }
        }

        // 5. Nếu được cấp ít nhất 1 lượt, sinh GameAccessToken
        if (totalTurnsGrantedAcrossCampaigns > 0) {
            GameAccessToken token = new GameAccessToken();
            token.setToken(UUID.randomUUID().toString());
            token.setMaHoaDon(invoice.getMaHoaDon());
            token.setSoLuongLuotThuong(totalTurnsGrantedAcrossCampaigns);
            token.setDaSuDung(false);
            token.setMaKhachHangKichHoat(customer.getMaKhachHang());
            token.setHetHanLuc(LocalDateTime.now().plusDays(30)); // 30 ngày hết hạn
            return tokenRepo.save(token);
        }

        return null;
    }

    private Invoice saveInvoice(InvoiceRequestDTO request, String maKhachHang) throws Exception {
        Invoice invoice = new Invoice();
        invoice.setMaHoaDon(request.getInvoiceNumber());
        invoice.setMaHoaDonGoc(request.getOriginalInvoiceNumber());
        invoice.setMaStore(request.getMaStore());
        invoice.setMaKhachHang(maKhachHang);
        invoice.setTongTien(request.getTotalAmount());
        invoice.setPhuongThucTt(request.getPaymentMethod());
        invoice.setSanPhamJson(objectMapper.writeValueAsString(request.getSkuList()));
        invoice.setDaXuLy(true);
        return invoiceRepo.save(invoice);
    }
}
