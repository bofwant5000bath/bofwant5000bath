package com.example.backend.dto;

import com.example.backend.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Integer userId;
    private String username;
    private String fullName;
    private String profilePictureUrl;
    private String createdAt;

    public UserDto(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.profilePictureUrl = user.getProfilePictureUrl();
        this.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
    }
}