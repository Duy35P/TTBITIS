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
    private final ObjectMapper objectMapper;

    public TurnManagementService(CustomerRepository customerRepo, InvoiceRepository invoiceRepo, CampaignStoreRepository campaignStoreRepo, DeltaRuleEngine ruleEngine, CustomerTurnRepository customerTurnRepo, TurnTransactionRepository turnTransactionRepo, GameAccessTokenRepository tokenRepo, ObjectMapper objectMapper) {
        this.customerRepo = customerRepo;
        this.invoiceRepo = invoiceRepo;
        this.campaignStoreRepo = campaignStoreRepo;
        this.ruleEngine = ruleEngine;
        this.customerTurnRepo = customerTurnRepo;
        this.turnTransactionRepo = turnTransactionRepo;
        this.tokenRepo = tokenRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public GameAccessToken processInvoice(InvoiceRequestDTO request) throws Exception {
        // 1. Tìm hoặc tạo Customer
        Customer customer = customerRepo.findByPhone(request.getCustomerPhone())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
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
            saveInvoice(request, customer.getCustomerId());
            return null; // Không trả về token
        }

        // 3. Lưu Invoice mới
        Invoice invoice = saveInvoice(request, customer.getCustomerId());

        // 4. Lấy danh sách Campaign đang chạy tại Store này
        List<CampaignStore> campaigns = campaignStoreRepo.findByIdCuaHang(request.getStoreId());
        
        int totalTurnsGrantedAcrossCampaigns = 0;

        for (CampaignStore cs : campaigns) {
            Long campaignId = cs.getIdChienDich();
            int turns = ruleEngine.calculateTurns(campaignId, deltaAmount, request.getPaymentMethod(), request.getSkuList());

            if (turns > 0) {
                // Pessimistic Lock
                CustomerTurn turn = customerTurnRepo.findByIdKhachHangAndIdChienDich(customer.getCustomerId(), campaignId)
                        .orElseGet(() -> {
                            CustomerTurn newTurn = new CustomerTurn();
                            newTurn.setIdKhachHang(customer.getCustomerId());
                            newTurn.setIdChienDich(campaignId);
                            newTurn.setLuotConLai(0);
                            return customerTurnRepo.save(newTurn);
                        });

                turn.setLuotConLai(turn.getLuotConLai() + turns);
                customerTurnRepo.save(turn);

                TurnTransaction transaction = new TurnTransaction();
                transaction.setIdKhachHang(customer.getCustomerId());
                transaction.setIdChienDich(campaignId);
                transaction.setLoai(1); // 1 = Cộng lượt
                transaction.setSoLuong(turns);
                transaction.setNguonThamChieu("INVOICE:" + invoice.getMaHoaDon());
                turnTransactionRepo.save(transaction);

                totalTurnsGrantedAcrossCampaigns += turns;
            }
        }

        // 5. Nếu được cấp ít nhất 1 lượt, sinh GameAccessToken
        if (totalTurnsGrantedAcrossCampaigns > 0) {
            GameAccessToken token = new GameAccessToken();
            token.setToken(UUID.randomUUID().toString());
            token.setIdHoaDon(invoice.getInvoiceId());
            token.setSoLuongLuotThuong(totalTurnsGrantedAcrossCampaigns);
            token.setDaSuDung(false);
            token.setIdKhachHangKichHoat(customer.getCustomerId());
            token.setHetHanLuc(LocalDateTime.now().plusDays(30)); // 30 ngày hết hạn
            return tokenRepo.save(token);
        }

        return null;
    }

    private Invoice saveInvoice(InvoiceRequestDTO request, Long customerId) throws Exception {
        Invoice invoice = new Invoice();
        invoice.setMaHoaDon(request.getInvoiceNumber());
        invoice.setMaHoaDonGoc(request.getOriginalInvoiceNumber());
        invoice.setIdCuaHang(request.getStoreId());
        invoice.setIdKhachHang(customerId);
        invoice.setTongTien(request.getTotalAmount());
        invoice.setPhuongThucTt(request.getPaymentMethod());
        invoice.setSanPhamJson(objectMapper.writeValueAsString(request.getSkuList()));
        invoice.setDaXuLy(true);
        return invoiceRepo.save(invoice);
    }
}
