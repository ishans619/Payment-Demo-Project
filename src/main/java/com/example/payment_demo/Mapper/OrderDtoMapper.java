package com.example.payment_demo.Mapper;

import com.example.payment_demo.dto.OrderResponseDto;
import com.example.payment_demo.model.OrderEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderDtoMapper {

    public OrderResponseDto toDto(OrderEntity order){
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setAmount(order.getAmount());
        dto.setProductName(order.getProductName());
        dto.setStatus(order.getStatus());
        return dto;
    }


    public OrderEntity toEntity(OrderResponseDto dto){
        OrderEntity entity = new OrderEntity();
        entity.setId(dto.getId());
        entity.setAmount(dto.getAmount());
        entity.setProductName(dto.getProductName());
        entity.setStatus(dto.getStatus());
        return entity;
    }

}
