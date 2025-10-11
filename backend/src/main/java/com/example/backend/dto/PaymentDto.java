package com.example.backend.dto;

import com.example.backend.model.Payment;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentDto {

    private Integer paymentId;
    private Integer billId;
    private UserDto payerUser; // ใช้ UserDto ที่มีอยู่แล้ว
    private BigDecimal amount;
    private LocalDateTime paymentDate;

    public PaymentDto(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.billId = payment.getBill().getBillId();
        this.payerUser = new UserDto(payment.getPayerUser());
        this.amount = payment.getAmount();
        this.paymentDate = payment.getPaymentDate();
    }
}