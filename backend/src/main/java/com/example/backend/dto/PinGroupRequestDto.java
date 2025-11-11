package com.example.backend.dto;

public class PinGroupRequestDto {
    private Integer userId;
    private Integer groupId;
    private boolean pin; // true = pin, false = unpin

    // Getters
    public Integer getUserId() {
        return userId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public boolean isPin() {
        return pin;
    }

    // Setters
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public void setPin(boolean pin) {
        this.pin = pin;
    }
}