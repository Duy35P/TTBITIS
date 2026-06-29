package com.bitis.luckydraw.dto;

import lombok.Data;
import java.util.List;

@Data
public class PosSyncRequest {
    private String maStore; // Mock field for UI selection
    private String invoiceCode;
    private String customerPhone;
    private String originalInvoiceCode;
    private Double totalAmount;
    private String paymentMethod;
    private List<PosSyncSku> skus;

    @Data
    public static class PosSyncSku {
        private String skuCode;
        private String name;
        private Integer quantity;
        private Double amount;
    }
}
