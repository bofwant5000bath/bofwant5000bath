package com.example.backend.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GroupMemberTest {

    @Mock
    private Group mockGroup; // "นักแสดงแทน" ของ Group

    @Mock
    private User mockUser; // "นักแสดงแทน" ของ User

    @Test
    void testGettersAndSetters() {
        // Arrange
        GroupMember groupMember = new GroupMember();

        // Act
        groupMember.setGroup(mockGroup);
        groupMember.setUser(mockUser);

        // Assert
        assertEquals(mockGroup, groupMember.getGroup());
        assertEquals(mockUser, groupMember.getUser());
    }
}