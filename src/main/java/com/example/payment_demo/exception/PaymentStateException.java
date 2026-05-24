package com.example.payment_demo.exception;

public class PaymentStateException extends RuntimeException{
    public PaymentStateException(String message){
        super(message);
    }
}
