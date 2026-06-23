package com.bitis.luckydraw.dto;

import lombok.Data;
import java.util.List;

@Data
public class PosSyncRequest {
    private Long storeId; // Mock field for UI selection
    private String invoiceCode;
    private String customerPhone;
    private Double totalAmount;
    private String paymentMethod;
    private List<String> skus;
}
