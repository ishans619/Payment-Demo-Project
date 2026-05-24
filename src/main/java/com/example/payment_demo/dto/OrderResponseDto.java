package com.example.payment_demo.dto;

public class OrderResponseDto {

    private Long id;
    private String productName;
    private Integer amount;
    private String status;

    public OrderResponseDto() {
    }

    public OrderResponseDto(Long id, String productName, Integer amount, String status) {
        this.id = id;
        this.productName = productName;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
