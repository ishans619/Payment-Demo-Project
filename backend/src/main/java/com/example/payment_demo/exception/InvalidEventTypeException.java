package com.example.payment_demo.exception;

public class InvalidEventTypeException extends RuntimeException{
    public InvalidEventTypeException(String message){
        super(message);
    }
}
