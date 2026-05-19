package com.example.payment_demo.controller;

import com.example.payment_demo.model.OrderEntity;
import com.example.payment_demo.model.PaymentEntity;
import com.example.payment_demo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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


}
