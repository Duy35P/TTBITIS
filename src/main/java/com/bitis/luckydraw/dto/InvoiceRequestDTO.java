package com.bitis.luckydraw.dto;

import java.math.BigDecimal;
import java.util.List;

public class InvoiceRequestDTO {

    private String invoiceNumber;
    private String originalInvoiceNumber;
    private Double totalAmount;
    private String paymentMethod;
    private String customerPhone;
    private Long storeId;
    private List<SkuItem> skuList;

    public static class SkuItem {
        private String sku;
        private Integer quantity;

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

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

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public List<SkuItem> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<SkuItem> skuList) {
        this.skuList = skuList;
    }
}
