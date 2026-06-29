package com.bitis.luckydraw.service;

import com.bitis.luckydraw.dto.PosSyncRequest;
import com.bitis.luckydraw.dto.PosSyncResponse;
import com.bitis.luckydraw.model.*;
import com.bitis.luckydraw.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PosService {

    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignStoreRepository campaignStoreRepository;
    private final CampaignRuleRepository campaignRuleRepository;
    private final CampaignRulePaymentRepository campaignRulePaymentRepository;
    private final CampaignRuleSkuRepository campaignRuleSkuRepository;
    private final CustomerTurnRepository customerTurnRepository;

    public PosService(CustomerRepository customerRepository, InvoiceRepository invoiceRepository,
                      CampaignRepository campaignRepository, CampaignStoreRepository campaignStoreRepository,
                      CampaignRuleRepository campaignRuleRepository, CampaignRulePaymentRepository campaignRulePaymentRepository,
                      CampaignRuleSkuRepository campaignRuleSkuRepository, CustomerTurnRepository customerTurnRepository) {
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.campaignRepository = campaignRepository;
        this.campaignStoreRepository = campaignStoreRepository;
        this.campaignRuleRepository = campaignRuleRepository;
        this.campaignRulePaymentRepository = campaignRulePaymentRepository;
        this.campaignRuleSkuRepository = campaignRuleSkuRepository;
        this.customerTurnRepository = customerTurnRepository;
    }

    @Transactional
    public PosSyncResponse processInvoice(PosSyncRequest request) {
        // 1. Ensure Customer exists
        String phone = request.getCustomerPhone();
        if (phone == null || phone.trim().isEmpty()) {
            return PosSyncResponse.builder().status("ERROR").message("Số điện thoại không được để trống").build();
        }
        
        Customer customer = customerRepository.findByPhone(phone).orElseGet(() -> {
            Customer newCustomer = new Customer();
            newCustomer.setMaKhachHang("CUS-" + phone);
            newCustomer.setPhone(phone);
            newCustomer.setTenKhach("Khách hàng " + phone);
            newCustomer.setTrangThai(1);
            return customerRepository.save(newCustomer);
        });

        // 2. Save Invoice
        if (request.getInvoiceCode() != null && !request.getInvoiceCode().isEmpty()) {
            if (request.getOriginalInvoiceCode() != null && !request.getOriginalInvoiceCode().isEmpty()) {
                if (invoiceRepository.existsByMaHoaDonGoc(request.getOriginalInvoiceCode())) {
                    return PosSyncResponse.builder().status("ERROR").message("Mã hóa đơn gốc đã được sử dụng để đổi hàng trước đó!").build();
                }
            }
            
            Optional<Invoice> existingInvoice = invoiceRepository.findByMaHoaDon(request.getInvoiceCode());
            if (existingInvoice.isPresent()) {
                return PosSyncResponse.builder().status("ERROR").message("Hóa đơn đã được đồng bộ trước đó!").build();
            }
            Invoice invoice = new Invoice();
            invoice.setMaStore(request.getMaStore());
            invoice.setMaKhachHang(customer.getMaKhachHang());
            invoice.setMaHoaDon(request.getInvoiceCode());
            invoice.setMaHoaDonGoc(request.getOriginalInvoiceCode());
            invoice.setTongTien(request.getTotalAmount());
            invoice.setPhuongThucTt(request.getPaymentMethod());
            invoice.setDaXuLy(true); // Mặc định hợp lệ
            
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String skusJson = mapper.writeValueAsString(request.getSkus());
                invoice.setSanPhamJson(skusJson);
            } catch (Exception e) {
                invoice.setSanPhamJson("[]");
            }
            
            invoiceRepository.save(invoice);
        }

        // 3. Process Campaigns
        Double deltaAmount = request.getTotalAmount();
        if (request.getOriginalInvoiceCode() != null && !request.getOriginalInvoiceCode().isEmpty()) {
            Optional<Invoice> originalOpt = invoiceRepository.findByMaHoaDon(request.getOriginalInvoiceCode());
            if (originalOpt.isPresent()) {
                deltaAmount = request.getTotalAmount() - originalOpt.get().getTongTien();
            }
        }
        
        List<CampaignStore> storeCampaigns = campaignStoreRepository.findByMaStore(request.getMaStore());
        LocalDateTime now = LocalDateTime.now();
        List<String> appliedCampaigns = new ArrayList<>();
        int totalTurnsEarned = 0;

        for (CampaignStore cs : storeCampaigns) {
            Optional<Campaign> optCampaign = campaignRepository.findByMaChienDich(cs.getMaChienDich());
            if (optCampaign.isPresent()) {
                Campaign campaign = optCampaign.get();
                if (campaign.getTrangThai() == 1 &&
                    campaign.getNgayBatDau().isBefore(now) &&
                    campaign.getNgayKetThuc().isAfter(now)) {
                    
                    if (deltaAmount > 0) {
                        int turnsForThisCampaign = calculateTurns(campaign.getMaChienDich(), request, deltaAmount);
                        if (turnsForThisCampaign > 0) {
                            // Call Stored Procedure
                            customerTurnRepository.addCustomerTurnsSafe(
                                customer.getMaKhachHang(),
                                campaign.getMaChienDich(),
                                turnsForThisCampaign,
                                request.getInvoiceCode() != null ? request.getInvoiceCode() : "POS-SYNC"
                            );
                            appliedCampaigns.add(campaign.getTenChienDich());
                            totalTurnsEarned += turnsForThisCampaign;
                        }
                    }
                }
            }
        }

        if (totalTurnsEarned > 0) {
            return PosSyncResponse.builder()
                .status("SUCCESS")
                .message("Đã cộng thành công " + totalTurnsEarned + " lượt!")
                .appliedCampaigns(appliedCampaigns)
                .totalTurns(totalTurnsEarned)
                .build();
        } else {
            return PosSyncResponse.builder()
                .status("SUCCESS")
                .message("Hóa đơn hợp lệ nhưng không đủ điều kiện nhận lượt từ chiến dịch nào.")
                .appliedCampaigns(appliedCampaigns)
                .totalTurns(0)
                .build();
        }
    }

    private int calculateTurns(String maChienDich, PosSyncRequest request, Double deltaAmount) {
        int turns = 0;

        // 1. Basic Rule
        Optional<CampaignRule> optRule = campaignRuleRepository.findByMaChienDich(maChienDich);
        if (optRule.isPresent() && optRule.get().getGiaTriDonHangToiThieu() != null && optRule.get().getGiaTriDonHangToiThieu() > 0) {
            Double minOrderValue = optRule.get().getGiaTriDonHangToiThieu();
            if (deltaAmount != null && deltaAmount >= minOrderValue) {
                turns += (int) (deltaAmount / minOrderValue);
            }
        }

        // 2. Payment Rule
        if (request.getPaymentMethod() != null) {
            List<CampaignRulePayment> payments = campaignRulePaymentRepository.findByMaChienDich(maChienDich);
            for (CampaignRulePayment p : payments) {
                if (p.getPhuongThucThanhToan().equalsIgnoreCase(request.getPaymentMethod())) {
                    turns += p.getSoLuotThuong();
                }
            }
        }

        // 3. SKU Rule
        if (request.getSkus() != null && !request.getSkus().isEmpty()) {
            List<CampaignRuleSku> skuRules = campaignRuleSkuRepository.findByMaChienDich(maChienDich);
            for (PosSyncRequest.PosSyncSku purchasedSku : request.getSkus()) {
                for (CampaignRuleSku rule : skuRules) {
                    if (rule.getMaSku().equalsIgnoreCase(purchasedSku.getSkuCode())) {
                        int qty = purchasedSku.getQuantity() != null ? purchasedSku.getQuantity() : 1;
                        turns += rule.getSoLuotThuong() * qty;
                    }
                }
            }
        }

        return turns;
    }
}
