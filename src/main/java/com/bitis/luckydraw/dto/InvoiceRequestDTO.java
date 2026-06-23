package com.bitis.luckydraw.dto;

import java.math.BigDecimal;
import java.util.List;

public class InvoiceRequestDTO {

    private String invoiceNumber;
    private String originalInvoiceNumber;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private Long customerId;
    private Long storeId;
    private List<String> skuList;

    // Getters and Setters
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getOriginalInvoiceNumber() {
        return originalInvoiceNumber;
    }

    public void setOriginalInvoiceNumber(String originalInvoiceNumber) {
        this.originalInvoiceNumber = originalInvoiceNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public List<String> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<String> skuList) {
        this.skuList = skuList;
    }
}
