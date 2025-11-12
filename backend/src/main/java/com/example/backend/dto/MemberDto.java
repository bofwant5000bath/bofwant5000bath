package com.example.backend.dto;

public class MemberDto {
    private Integer userId;
    private String fullName;
    // คุณอาจเพิ่ม profilePictureUrl ที่นี่ได้ในอนาคต

    public MemberDto(Integer userId, String fullName) {
        this.userId = userId;
        this.fullName = fullName;
    }

    // Getters
    public Integer getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    // Setters (เผื่อไว้)
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}