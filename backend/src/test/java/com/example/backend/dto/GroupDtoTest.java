package com.example.backend.dto;

import com.example.backend.model.Group;
import com.example.backend.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupDtoTest {

    @Mock
    private Group mockGroup;
    @Mock
    private User mockCreatedByUser;

    @Test
    void constructor_ShouldMapAllFieldsFromGroup() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        when(mockGroup.getGroupId()).thenReturn(10);
        when(mockGroup.getGroupName()).thenReturn("Project Phoenix");
        when(mockGroup.getCreatedByUser()).thenReturn(mockCreatedByUser); // return a mock
        when(mockGroup.getCreatedAt()).thenReturn(now);

        // กำหนดบทบาทให้ mock user ที่ซ้อนอยู่
        when(mockCreatedByUser.getUserId()).thenReturn(1);
        when(mockCreatedByUser.getUsername()).thenReturn("admin");

        // Act
        GroupDto groupDto = new GroupDto(mockGroup);

        // Assert
        assertEquals(10, groupDto.getGroupId());
        assertEquals("Project Phoenix", groupDto.getGroupName());
        assertEquals(now.toString(), groupDto.getCreatedAt());
        assertNotNull(groupDto.getCreatedByUser());
        assertEquals(1, groupDto.getCreatedByUser().getUserId());
        assertEquals("admin", groupDto.getCreatedByUser().getUsername());
    }
}