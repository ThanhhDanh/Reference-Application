package com.example.referenceapp.model;

import java.io.Serializable;

public class Orders implements Serializable {
    private int Id;
    private int DocumentId;
    private int UserId;
    private int Amount;
    private double FeeDelivery;
    private double SubTotal;
    private double Total;
    private double TotalTax;
    private String PaymentMethod;
    private String CreatedAt;

    public Orders() {
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getDocumentId() {
        return DocumentId;
    }

    public void setDocumentId(int documentId) {
        DocumentId = documentId;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public int getAmount() {
        return Amount;
    }

    public void setAmount(int amount) {
        Amount = amount;
    }

    public double getFeeDelivery() {
        return FeeDelivery;
    }

    public void setFeeDelivery(double feeDelivery) {
        FeeDelivery = feeDelivery;
    }

    public double getSubTotal() {
        return SubTotal;
    }

    public void setSubTotal(double subTotal) {
        SubTotal = subTotal;
    }

    public double getTotal() {
        return Total;
    }

    public void setTotal(double total) {
        Total = total;
    }

    public double getTotalTax() {
        return TotalTax;
    }

    public void setTotalTax(double totalTax) {
        TotalTax = totalTax;
    }

    public String getPaymentMethod() {
        return PaymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        PaymentMethod = paymentMethod;
    }

    public String getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        CreatedAt = createdAt;
    }
}
