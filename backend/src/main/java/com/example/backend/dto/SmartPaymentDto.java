package com.example.backend.dto;

import java.math.BigDecimal;

// (แนะนำให้ใช้ @Getter/@Setter)
public class SmartPaymentDto {

    private UserDto fromUser;
    private UserDto toUser;
    private BigDecimal amount;

    public SmartPaymentDto(UserDto fromUser, UserDto toUser, BigDecimal amount) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amount = amount;
    }

    // Getters and Setters
    public UserDto getFromUser() { return fromUser; }
    public void setFromUser(UserDto fromUser) { this.fromUser = fromUser; }
    public UserDto getToUser() { return toUser; }
    public void setToUser(UserDto toUser) { this.toUser = toUser; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}