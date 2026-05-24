package com.example.payment_demo.dto;

public class PaymentWebhookRequest {

    private String paymentReference;
    private String eventType;

    public PaymentWebhookRequest() {
    }

    public PaymentWebhookRequest(String paymentReference, String eventType) {
        this.paymentReference = paymentReference;
        this.eventType = eventType;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
