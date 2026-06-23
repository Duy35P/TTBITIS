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
            newCustomer.setPhone(phone);
            newCustomer.setTenKhach("Khách hàng " + phone);
            newCustomer.setTrangThai(1);
            return customerRepository.save(newCustomer);
        });

        // 2. Save Invoice
        if (request.getInvoiceCode() != null && !request.getInvoiceCode().isEmpty()) {
            Optional<Invoice> existingInvoice = invoiceRepository.findByMaHoaDon(request.getInvoiceCode());
            if (existingInvoice.isPresent()) {
                return PosSyncResponse.builder().status("ERROR").message("Hóa đơn đã được đồng bộ trước đó!").build();
            }
            Invoice invoice = new Invoice();
            invoice.setIdCuaHang(request.getStoreId());
            invoice.setIdKhachHang(customer.getCustomerId());
            invoice.setMaHoaDon(request.getInvoiceCode());
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
        List<CampaignStore> storeCampaigns = campaignStoreRepository.findByIdCuaHang(request.getStoreId());
        LocalDateTime now = LocalDateTime.now();
        List<String> appliedCampaigns = new ArrayList<>();
        int totalTurnsEarned = 0;

        for (CampaignStore cs : storeCampaigns) {
            Optional<Campaign> optCampaign = campaignRepository.findById(cs.getIdChienDich());
            if (optCampaign.isPresent()) {
                Campaign campaign = optCampaign.get();
                if (campaign.getTrangThai() == 1 &&
                    campaign.getNgayBatDau().isBefore(now) &&
                    campaign.getNgayKetThuc().isAfter(now)) {
                    
                    int turnsForThisCampaign = calculateTurns(campaign.getCampaignId(), request);
                    if (turnsForThisCampaign > 0) {
                        // Call Stored Procedure
                        customerTurnRepository.addCustomerTurnsSafe(
                            customer.getCustomerId(),
                            campaign.getCampaignId(),
                            turnsForThisCampaign,
                            request.getInvoiceCode() != null ? request.getInvoiceCode() : "POS-SYNC"
                        );
                        appliedCampaigns.add(campaign.getTenChienDich());
                        totalTurnsEarned += turnsForThisCampaign;
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

    private int calculateTurns(Long campaignId, PosSyncRequest request) {
        int turns = 0;

        // 1. Basic Rule
        Optional<CampaignRule> optRule = campaignRuleRepository.findByIdChienDich(campaignId);
        if (optRule.isPresent() && optRule.get().getGiaTriDonHangToiThieu() != null && optRule.get().getGiaTriDonHangToiThieu() > 0) {
            Double minOrderValue = optRule.get().getGiaTriDonHangToiThieu();
            if (request.getTotalAmount() != null && request.getTotalAmount() >= minOrderValue) {
                turns += (int) (request.getTotalAmount() / minOrderValue);
            }
        }

        // 2. Payment Rule
        if (request.getPaymentMethod() != null) {
            List<CampaignRulePayment> payments = campaignRulePaymentRepository.findByIdChienDich(campaignId);
            for (CampaignRulePayment p : payments) {
                if (p.getPhuongThucThanhToan().equalsIgnoreCase(request.getPaymentMethod())) {
                    turns += p.getSoLuotThuong();
                }
            }
        }

        // 3. SKU Rule
        if (request.getSkus() != null && !request.getSkus().isEmpty()) {
            List<CampaignRuleSku> skuRules = campaignRuleSkuRepository.findByIdChienDich(campaignId);
            for (String purchasedSku : request.getSkus()) {
                for (CampaignRuleSku rule : skuRules) {
                    if (rule.getMaSku().equalsIgnoreCase(purchasedSku)) {
                        turns += rule.getSoLuotThuong();
                    }
                }
            }
        }

        return turns;
    }
}
