package com.example.payment_demo.service;

import com.example.payment_demo.Mapper.OrderDtoMapper;
import com.example.payment_demo.Mapper.PaymentDtoMapper;
import com.example.payment_demo.dto.OrderDetailsResponseDto;
import com.example.payment_demo.dto.OrderResponseDto;
import com.example.payment_demo.dto.PaymentResponseDto;
import com.example.payment_demo.dto.PaymentWebhookRequest;
import com.example.payment_demo.exception.InvalidEventTypeException;
import com.example.payment_demo.exception.PaymentRetryNotAllowedExeption;
import com.example.payment_demo.exception.PaymentStateException;
import com.example.payment_demo.exception.ResourceNotFoundException;
import com.example.payment_demo.model.OrderEntity;
import com.example.payment_demo.model.PaymentEntity;
import com.example.payment_demo.repository.OrderRepository;
import com.example.payment_demo.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderDtoMapper orderDtoMapper;
    @Autowired
    private PaymentDtoMapper paymentDtoMapper;

    public OrderResponseDto createOrder(String productName, Integer amount){
        OrderEntity order = new OrderEntity();
        order.setProductName(productName);
        order.setAmount(amount);
        order.setStatus("CREATED");

        orderRepository.save(order);
        OrderResponseDto dto = orderDtoMapper.toDto(order);
        return dto;
    }

    @Transactional
    public PaymentResponseDto createPayment(Long orderId, String idempotencyKey){
        Optional<PaymentEntity> existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);

        if(existingPayment.isPresent()){
            return paymentDtoMapper.toDto(existingPayment.get());
        }

        OrderEntity order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found with" +
                "id: " + orderId));

        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(order.getId());
        payment.setIdempotencyKey(idempotencyKey);
        payment.setPaymentReference("PAY_" + UUID.randomUUID());
        payment.setStatus("PROCESSING");

        order.setStatus("PAYMENT_PENDING");
        orderRepository.save(order);

        paymentRepository.save(payment);
        PaymentResponseDto dto = paymentDtoMapper.toDto(payment);
        return dto;
    }

    @Transactional
    public PaymentResponseDto handleWebhook(PaymentWebhookRequest request){
        PaymentEntity payment = paymentRepository.findByPaymentReference(request.getPaymentReference())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + request.getPaymentReference()));

        OrderEntity order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + payment.getOrderId()));

        if("SUCCESS".equals(payment.getStatus()) || "FAILED".equals(payment.getStatus())){
            throw new PaymentStateException("Payment is already in a final state");
        }

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
            throw new InvalidEventTypeException("Unsupported event type: " + request.getEventType());
        }

        orderRepository.save(order);
        paymentRepository.save(payment);

        PaymentResponseDto paymentDto = paymentDtoMapper.toDto(payment);
        return paymentDto;
    }

    public OrderResponseDto getOrderById(Long id){
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id : " + id));
        return orderDtoMapper.toDto(order);
    }

    public PaymentResponseDto getPaymentByReference(String paymentReference){
        PaymentEntity payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + paymentReference));
        return paymentDtoMapper.toDto(payment);
    }

    public OrderDetailsResponseDto getOrderDetails(Long orderId){
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderResponseDto orderDto = orderDtoMapper.toDto(order);

        PaymentEntity payment = paymentRepository.findFirstByOrderId(orderId).orElse(null);

        PaymentResponseDto paymentDto = null;

        if(payment != null){
            paymentDto = paymentDtoMapper.toDto(payment);
        }

        return new OrderDetailsResponseDto(orderDto, paymentDto);
    }

    public List<PaymentResponseDto> getPaymentsByOrderId(Long orderId){
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id:" + orderId));

        List<PaymentEntity> payments = paymentRepository.findByOrderIdOrderByIdDesc(order.getId());

        return payments.stream().map(paymentDtoMapper::toDto).toList();
    }

    @Transactional
    public PaymentResponseDto retryPayment(Long orderId){
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order id: " + orderId));

        Optional<PaymentEntity> lastPaymentOpt = paymentRepository.findFirstByOrderIdOrderByIdDesc(orderId);

        System.out.println("Last payment found: " + lastPaymentOpt.isPresent());
        if (lastPaymentOpt.isPresent()) {
            System.out.println("Last payment status: " + lastPaymentOpt.get().getStatus());
        }

        if(lastPaymentOpt.isEmpty()){
            throw new PaymentRetryNotAllowedExeption(
                    "No previous payment found for order. Use create payment instead."
            );
        }

        PaymentEntity lastPayment = lastPaymentOpt.get();

        if("SUCCESS".equals(lastPayment.getStatus())){
            throw new PaymentRetryNotAllowedExeption(
                    "Payment already succeeded. Retry is not allowed."
            );
        }

        if("PROCESSING".equals(lastPayment.getStatus())){
            throw new PaymentRetryNotAllowedExeption(
                    "Payment is already in processing state. Wait for it to get completed."
            );
        }

        PaymentEntity retryPayment = new PaymentEntity();
        retryPayment.setOrderId(orderId);
        retryPayment.setIdempotencyKey("RETRY_" + UUID.randomUUID());
        retryPayment.setPaymentReference("PAY_" + UUID.randomUUID());
        retryPayment.setStatus("PROCESSING");

        order.setStatus("PAYMENT_PENDING");
        orderRepository.save(order);

        return paymentDtoMapper.toDto(paymentRepository.save(retryPayment));

    }
}
