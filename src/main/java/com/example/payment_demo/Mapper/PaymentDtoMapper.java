package com.example.payment_demo.Mapper;

import com.example.payment_demo.dto.PaymentResponseDto;
import com.example.payment_demo.model.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentDtoMapper {

    public PaymentResponseDto toDto(PaymentEntity entity){
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(entity.getId());
        dto.setOrderId(entity.getOrderId());
        dto.setIdempotencyKey(entity.getIdempotencyKey());
        dto.setPaymentReference(entity.getPaymentReference());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public PaymentEntity toEntity(PaymentResponseDto dto){
        PaymentEntity entity = new PaymentEntity();
        entity.setId(dto.getId());
        entity.setOrderId(dto.getOrderId());
        entity.setIdempotencyKey(dto.getIdempotencyKey());
        entity.setPaymentReference(dto.getPaymentReference());
        entity.setStatus(dto.getStatus());
        return entity;
    }
}
