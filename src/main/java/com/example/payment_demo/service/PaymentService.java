package com.example.payment_demo.service;

import com.example.payment_demo.dto.PaymentWebhookRequest;
import com.example.payment_demo.model.OrderEntity;
import com.example.payment_demo.model.PaymentEntity;
import com.example.payment_demo.repository.OrderRepository;
import com.example.payment_demo.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    public OrderEntity createOrder(String productName, Integer amount){
        OrderEntity order = new OrderEntity();
        order.setProductName(productName);
        order.setAmount(amount);
        order.setStatus("CREATED");
        return orderRepository.save(order);
    }

    public PaymentEntity createPayment(Long orderId, String idempotencyKey){
        Optional<PaymentEntity> existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);

        if(existingPayment.isPresent()){
            return existingPayment.get();
        }

        OrderEntity order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(order.getId());
        payment.setIdempotencyKey(idempotencyKey);
        payment.setPaymentReference("PAY_" + UUID.randomUUID());
        payment.setStatus("POCESSING");

        order.setStatus("PAYMENT_PENDING");
        orderRepository.save(order);

        return paymentRepository.save(payment);
    }

    public PaymentEntity handleWebhook(PaymentWebhookRequest request){
        PaymentEntity payment = paymentRepository.findByPaymentReference(request.getPaymentReference())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        OrderEntity order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if("payment_intent.succeeded".equals(request.getEventType())){
            payment.setStatus("SUCCESS");
            order.setStatus("PAID");
        }
        else if("payment_intent.payment_failed".equals(request.getEventType()) || "payment_intent.failed".equals(request.getEventType())){
            payment.setStatus("FAILED");
            order.setStatus("PAYMENT_FAILED");
        }
        else if("payment_intent.processing".equals(request.getEventType())){
            payment.setStatus("PROCESSING");
            order.setStatus("PAYMENT_PENDING");
        }
        else{
            throw new IllegalArgumentException("Unsupported event type: " + request.getEventType());
        }

        orderRepository.save(order);
        return paymentRepository.save(payment);
    }
}
