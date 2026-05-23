package com.example.payment_demo.repository;

import com.example.payment_demo.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);
    List<PaymentEntity> findByOrderId(Long orderId);
    Optional<PaymentEntity> findByPaymentReference(String paymentReference);
    Optional<PaymentEntity> findFirstByOrderId(Long orderId);
    List<PaymentEntity> findByOrderIdOrderByIdDesc(Long orderId);
    Optional<PaymentEntity> findFirstByOrderIdOrderByIdDesc(Long orderId);

}
