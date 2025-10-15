package com.example.backend.dto;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreateGroupRequestTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        CreateGroupRequest request = new CreateGroupRequest();
        List<Integer> memberIds = Arrays.asList(1, 2, 3);

        // Act
        request.setGroupName("Summer Trip");
        request.setCreatedByUserId(1);
        request.setMemberIds(memberIds);

        // Assert
        assertEquals("Summer Trip", request.getGroupName());
        assertEquals(1, request.getCreatedByUserId());
        assertEquals(3, request.getMemberIds().size());
        assertEquals(memberIds, request.getMemberIds());
    }
}