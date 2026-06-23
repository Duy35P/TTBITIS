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
    public PosSyncResponse syncInvoice(PosSyncRequest request) {
        // 1. Resolve Customer
        Customer customer = customerRepository.findByPhone(request.getSoDienThoai())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setPhone(request.getSoDienThoai());
                    newCustomer.setTenKhach(request.getTenKhachHang());
                    newCustomer.setTrangThai(1);
                    return customerRepository.save(newCustomer);
                });

        // 2. Save Invoice
        Invoice invoice = new Invoice();
        invoice.setIdKhachHang(customer.getCustomerId());
        invoice.setIdCuaHang(request.getStoreId());
        invoice.setTongTien(request.getTongTien());
        String maHoaDon = "INV-" + System.currentTimeMillis();
        invoice.setMaHoaDon(maHoaDon);
        invoiceRepository.save(invoice);

        // 3. Process Campaigns
        List<CampaignStore> storeLinks = campaignStoreRepository.findByIdCuaHang(request.getStoreId());
        int totalEarnedTurns = 0;
        List<String> appliedCampaigns = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (CampaignStore cs : storeLinks) {
            Campaign campaign = campaignRepository.findById(cs.getIdChienDich()).orElse(null);
            if (campaign != null) {
                // Check if campaign is active and within date range
                if (campaign.getTrangThai() == 1 &&
                        !now.isBefore(campaign.getNgayBatDau()) &&
                        !now.isAfter(campaign.getNgayKetThuc())) {

                    int turnsForThisCampaign = calculateTurns(campaign.getCampaignId(), request);

                    if (turnsForThisCampaign > 0) {
                        // Add turns via Stored Procedure
                        customerTurnRepository.addCustomerTurnsSafe(
                                customer.getCustomerId(),
                                campaign.getCampaignId(),
                                turnsForThisCampaign,
                                "Hóa đơn POS: " + maHoaDon
                        );
                        totalEarnedTurns += turnsForThisCampaign;
                        appliedCampaigns.add(campaign.getTenChienDich() + " (+" + turnsForThisCampaign + " lượt)");
                    }
                }
            }
        }

        PosSyncResponse response = new PosSyncResponse();
        if (totalEarnedTurns > 0) {
            response.setStatus("SUCCESS");
            response.setMessage("Hóa đơn đã được đồng bộ. Khách hàng nhận được " + totalEarnedTurns + " lượt quay.");
        } else {
            response.setStatus("NO_TURNS");
            response.setMessage("Hóa đơn đã được đồng bộ nhưng không thỏa điều kiện cộng lượt quay nào.");
        }
        response.setTotalTurns(totalEarnedTurns);
        response.setAppliedCampaigns(appliedCampaigns);

        return response;
    }

    private int calculateTurns(Long campaignId, PosSyncRequest request) {
        int turns = 0;

        // Rule 1: Basic Order Value
        CampaignRule basicRule = campaignRuleRepository.findByIdChienDich(campaignId).orElse(null);
        if (basicRule != null && basicRule.getGiaTriDonHangToiThieu() != null && basicRule.getGiaTriDonHangToiThieu() > 0) {
            if (request.getTongTien() != null && request.getTongTien() >= basicRule.getGiaTriDonHangToiThieu()) {
                turns += (int) (request.getTongTien() / basicRule.getGiaTriDonHangToiThieu());
            }
        }

        // Rule 2: Payment Method
        if (request.getPhuongThucThanhToan() != null) {
            List<CampaignRulePayment> paymentRules = campaignRulePaymentRepository.findByIdChienDich(campaignId);
            for (CampaignRulePayment pr : paymentRules) {
                if (pr.getPhuongThucThanhToan().equalsIgnoreCase(request.getPhuongThucThanhToan())) {
                    turns += pr.getSoLuotThuong();
                }
            }
        }

        // Rule 3: SKUs
        if (request.getDanhSachSku() != null && !request.getDanhSachSku().isEmpty()) {
            List<CampaignRuleSku> skuRules = campaignRuleSkuRepository.findByIdChienDich(campaignId);
            for (String sku : request.getDanhSachSku()) {
                for (CampaignRuleSku sr : skuRules) {
                    if (sr.getMaSku().equalsIgnoreCase(sku.trim())) {
                        turns += sr.getSoLuotThuong();
                    }
                }
            }
        }

        return turns;
    }
}
