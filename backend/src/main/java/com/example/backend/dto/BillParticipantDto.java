package com.example.backend.dto;

import com.example.backend.model.BillParticipant;
import com.example.backend.model.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BillParticipantDto {
    private UserDto user;
    private BigDecimal splitAmount;
    private PaymentStatus status; // ใช้ enum ตรง ๆ

    public BillParticipantDto(BillParticipant billParticipant) {
        this.user = new UserDto(billParticipant.getUser());
        this.splitAmount = billParticipant.getSplitAmount();
        this.status = billParticipant.getIsPaid(); // คืนค่า enum ได้เลย
    }
}
