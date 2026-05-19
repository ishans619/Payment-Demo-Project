package com.example.payment_demo.controller;

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
    public OrderEntity createOrder(@RequestParam String productName, @RequestParam Integer amount){
        return service.createOrder(productName, amount);
    }

    @PostMapping("/payments")
    public PaymentEntity createPayment(@RequestParam Long OrderId, @RequestParam String idempotencyKey){
        return service.createPayment(OrderId, idempotencyKey);
    }

    @PostMapping("/webhook")
    public ResponseEntity<PaymentEntity> handleWebhook(@RequestBody PaymentWebhookRequest request){
        PaymentEntity payment = service.handleWebhook(request);
        return new ResponseEntity<>(payment, HttpStatus.OK);
    }
}
