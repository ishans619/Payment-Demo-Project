package com.example.payment_demo.exception;

public class PaymentRetryNotAllowedExeption extends RuntimeException {
    public PaymentRetryNotAllowedExeption(String message){
        super(message);
    }
}
