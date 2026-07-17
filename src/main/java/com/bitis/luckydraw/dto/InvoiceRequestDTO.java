package com.bitis.luckydraw.dto;

import java.math.BigDecimal;
import java.util.List;

public class InvoiceRequestDTO {

    private String invoiceNumber;
    private String originalInvoiceNumber;
    private Double totalAmount;
    private String paymentMethod;
    private String customerPhone;
    private String maStore;
    private List<SkuItem> skuList;

    public static class SkuItem {
        @com.fasterxml.jackson.annotation.JsonAlias({"sku", "skuCode"})
        private String sku;
        private String name;
        private Integer quantity;
        private Double amount;

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
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

    public String getMaStore() {
        return maStore;
    }

    public void setMaStore(String maStore) {
        this.maStore = maStore;
    }

    public List<SkuItem> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<SkuItem> skuList) {
        this.skuList = skuList;
    }
}
