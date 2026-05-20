package com.example.payment_demo.controller;

import com.example.payment_demo.dto.OrderResponseDto;
import com.example.payment_demo.dto.PaymentResponseDto;
import com.example.payment_demo.dto.PaymentWebhookRequest;
import com.example.payment_demo.model.OrderEntity;
import com.example.payment_demo.model.PaymentEntity;
import com.example.payment_demo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentController {

    @Autowired
    private PaymentService service;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponseDto> createOrder(@RequestParam String productName, @RequestParam Integer amount){
        return new ResponseEntity<>(service.createOrder(productName, amount), HttpStatus.CREATED);
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponseDto> createPayment(@RequestParam Long orderId, @RequestParam String idempotencyKey){
        return new ResponseEntity<>(service.createPayment(orderId, idempotencyKey), HttpStatus.CREATED);
    }
    @PostMapping("/webhook")
    public ResponseEntity<PaymentResponseDto> handleWebhook(@RequestBody PaymentWebhookRequest request){
        PaymentResponseDto paymentdto = service.handleWebhook(request);
        return new ResponseEntity<>(paymentdto, HttpStatus.OK);
    }
}
