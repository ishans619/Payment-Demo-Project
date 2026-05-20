package com.example.payment_demo.dto;

public class PaymentResponseDto {

    private Long id;
    private Long orderId;
    private String paymentReference;
    private String idempotencyKey;
    private String status;

    public PaymentResponseDto() {
    }

    public PaymentResponseDto(Long id, Long orderId, String paymentReference, String idempotencyKey, String status) {
        this.id = id;
        this.orderId = orderId;
        this.paymentReference = paymentReference;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
