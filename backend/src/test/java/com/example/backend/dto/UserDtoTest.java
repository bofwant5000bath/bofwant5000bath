package com.example.backend.dto;

import com.example.backend.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDtoTest {

    @Mock
    private User mockUser;

    @Test
    void constructor_ShouldMapAllFieldsFromUser() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        when(mockUser.getUserId()).thenReturn(1);
        when(mockUser.getUsername()).thenReturn("john.doe");
        when(mockUser.getFullName()).thenReturn("John Doe");
        when(mockUser.getProfilePictureUrl()).thenReturn("http://example.com/profile.jpg");
        when(mockUser.getCreatedAt()).thenReturn(now);

        // Act
        UserDto userDto = new UserDto(mockUser);

        // Assert
        assertEquals(1, userDto.getUserId());
        assertEquals("john.doe", userDto.getUsername());
        assertEquals("John Doe", userDto.getFullName());
        assertEquals("http://example.com/profile.jpg", userDto.getProfilePictureUrl());
        assertEquals(now.toString(), userDto.getCreatedAt());
    }

    @Test
    void constructor_ShouldHandleNullCreatedAt() {
        // Arrange
        when(mockUser.getCreatedAt()).thenReturn(null);

        // Act
        UserDto userDto = new UserDto(mockUser);

        // Assert
        assertNull(userDto.getCreatedAt());
    }
}