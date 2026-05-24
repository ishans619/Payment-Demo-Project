package com.example.payment_demo.service;

import com.example.payment_demo.Mapper.PaymentDtoMapper;
import com.example.payment_demo.dto.PaymentResponseDto;
import com.example.payment_demo.dto.PaymentWebhookRequest;
import com.example.payment_demo.exception.PaymentRetryNotAllowedExeption;
import com.example.payment_demo.exception.ResourceNotFoundException;
import com.example.payment_demo.model.OrderEntity;
import com.example.payment_demo.model.PaymentEntity;
import com.example.payment_demo.repository.OrderRepository;
import com.example.payment_demo.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentDtoMapper paymentDtoMapper;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void retryPayment_shouldThrowException_whenOrderNotFound() {
        Long orderId = 1L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.retryPayment(orderId)
        );

        assertEquals("Order not found with order id: 1", ex.getMessage());
        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    void retryPayment_shouldThrowException_whenNoPreviousPaymentExists() {
        Long orderId = 1L;

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setProductName("Phone");
        order.setAmount(20000);
        order.setStatus("CREATED");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findFirstByOrderIdOrderByIdDesc(orderId)).thenReturn(Optional.empty());

        PaymentRetryNotAllowedExeption ex = assertThrows(
                PaymentRetryNotAllowedExeption.class,
                () -> paymentService.retryPayment(orderId)
        );

        assertEquals("No previous payment found for order. Use create payment instead.", ex.getMessage());

        verify(orderRepository).findById(orderId);
        verify(paymentRepository).findFirstByOrderIdOrderByIdDesc(orderId);
    }

    @Test
    void retryPayment_shouldThrowException_whenLastPaymentIsProcessing() {
        Long orderId = 1L;

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setStatus("PAYMENT_PENDING");

        PaymentEntity payment = new PaymentEntity();
        payment.setId(10L);
        payment.setOrderId(orderId);
        payment.setStatus("PROCESSING");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findFirstByOrderIdOrderByIdDesc(orderId)).thenReturn(Optional.of(payment));

        PaymentRetryNotAllowedExeption ex = assertThrows(
                PaymentRetryNotAllowedExeption.class,
                () -> paymentService.retryPayment(orderId)
        );

        assertEquals("Payment is already in processing state. Wait for it to get completed.", ex.getMessage());

        verify(orderRepository).findById(orderId);
        verify(paymentRepository).findFirstByOrderIdOrderByIdDesc(orderId);
    }

    @Test
    void retryPayment_shouldThrowException_whenLastPaymentIsSuccess() {
        Long orderId = 1L;

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setStatus("PAID");

        PaymentEntity payment = new PaymentEntity();
        payment.setId(10L);
        payment.setOrderId(orderId);
        payment.setStatus("SUCCESS");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findFirstByOrderIdOrderByIdDesc(orderId)).thenReturn(Optional.of(payment));

        PaymentRetryNotAllowedExeption ex = assertThrows(
                PaymentRetryNotAllowedExeption.class,
                () -> paymentService.retryPayment(orderId)
        );

        assertEquals("Payment already succeeded. Retry is not allowed.", ex.getMessage());

        verify(orderRepository).findById(orderId);
        verify(paymentRepository).findFirstByOrderIdOrderByIdDesc(orderId);
    }

    @Test
    void retryPayment_shouldCreateNewPayment_whenLastPaymentFailed() {
        Long orderId = 1L;

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setStatus("PAYMENT_FAILED");

        PaymentEntity failedPayment = new PaymentEntity();
        failedPayment.setId(1L);
        failedPayment.setOrderId(orderId);
        failedPayment.setStatus("FAILED");

        PaymentEntity savedRetryPayment = new PaymentEntity();
        savedRetryPayment.setId(2L);
        savedRetryPayment.setOrderId(orderId);
        savedRetryPayment.setStatus("PROCESSING");
        savedRetryPayment.setPaymentReference("PAY_NEW");
        savedRetryPayment.setIdempotencyKey("RETRY_123");

        PaymentResponseDto responseDto = new PaymentResponseDto(
                2L,
                orderId,
                "PAY_NEW",
                "RETRY_123",
                "PROCESSING"
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findFirstByOrderIdOrderByIdDesc(orderId)).thenReturn(Optional.of(failedPayment));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(savedRetryPayment);
        when(paymentDtoMapper.toDto(savedRetryPayment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.retryPayment(orderId);

        assertNotNull(result);
        assertEquals("PROCESSING", result.getStatus());
        assertEquals(orderId, result.getOrderId());
        assertEquals("PAY_NEW", result.getPaymentReference());

        verify(orderRepository).findById(orderId);
        verify(paymentRepository).findFirstByOrderIdOrderByIdDesc(orderId);
        verify(orderRepository).save(order);
        verify(paymentRepository).save(any(PaymentEntity.class));
        verify(paymentDtoMapper).toDto(savedRetryPayment);
    }

    @Test
    void handleWebhook_shouldMarkPaymentSuccess_andOrderPaid() {
        String paymentReference = "PAY_123";

        PaymentWebhookRequest request = new PaymentWebhookRequest();
        request.setPaymentReference(paymentReference);
        request.setEventType("payment_intent.succeeded");

        PaymentEntity payment = new PaymentEntity();
        payment.setId(1L);
        payment.setOrderId(1L);
        payment.setPaymentReference(paymentReference);
        payment.setStatus("PROCESSING");

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setStatus("PAYMENT_PENDING");

        PaymentResponseDto responseDto = new PaymentResponseDto(
                1L, 1L, paymentReference, "abc123", "SUCCESS"
        );

        when(paymentRepository.findByPaymentReference(paymentReference)).thenReturn(Optional.of(payment));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentDtoMapper.toDto(payment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.handleWebhook(request);

        assertEquals("SUCCESS", payment.getStatus());
        assertEquals("PAID", order.getStatus());
        assertEquals("SUCCESS", result.getStatus());

        verify(paymentRepository).findByPaymentReference(paymentReference);
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
        verify(paymentRepository).save(payment);
    }
}
