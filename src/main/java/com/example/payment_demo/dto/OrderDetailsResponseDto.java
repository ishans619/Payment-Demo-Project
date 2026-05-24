package com.example.payment_demo.dto;

public class OrderDetailsResponseDto {

    private OrderResponseDto orderResponseDto;
    private PaymentResponseDto paymentResponseDto;

    public OrderDetailsResponseDto() {
    }

    public OrderDetailsResponseDto(OrderResponseDto orderResponseDto, PaymentResponseDto paymentResponseDto) {
        this.orderResponseDto = orderResponseDto;
        this.paymentResponseDto = paymentResponseDto;
    }

    public OrderResponseDto getOrderResponseDto() {
        return orderResponseDto;
    }

    public void setOrderResponseDto(OrderResponseDto orderResponseDto) {
        this.orderResponseDto = orderResponseDto;
    }

    public PaymentResponseDto getPaymentResponseDto() {
        return paymentResponseDto;
    }

    public void setPaymentResponseDto(PaymentResponseDto paymentResponseDto) {
        this.paymentResponseDto = paymentResponseDto;
    }
}
