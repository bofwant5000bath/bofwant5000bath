package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreatePaymentRequest {
    private Integer billId;
    private Integer payerUserId;
    private BigDecimal amount;
    private String slipImageUrl;
}
