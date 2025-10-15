package com.example.backend.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        // Act
        user.setUserId(1);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setFullName("Test User");
        user.setProfilePictureUrl("http://example.com/image.png");
        user.setCreatedAt(now);

        // Assert
        assertEquals(1, user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("Test User", user.getFullName());
        assertEquals("http://example.com/image.png", user.getProfilePictureUrl());
        assertEquals(now, user.getCreatedAt());
    }
}