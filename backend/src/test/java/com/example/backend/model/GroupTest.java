package com.example.backend.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GroupTest {

    @Mock
    private User mockCreatedByUser; // "นักแสดงแทน" ของ User

    @Test
    void testGettersAndSetters() {
        // Arrange
        Group group = new Group();
        LocalDateTime now = LocalDateTime.now();

        // Act
        group.setGroupId(1);
        group.setGroupName("Vacation Planning");
        group.setCreatedByUser(mockCreatedByUser);
        group.setCreatedAt(now);

        // Assert
        assertEquals(1, group.getGroupId());
        assertEquals("Vacation Planning", group.getGroupName());
        assertEquals(mockCreatedByUser, group.getCreatedByUser());
        assertEquals(now, group.getCreatedAt());
    }
}