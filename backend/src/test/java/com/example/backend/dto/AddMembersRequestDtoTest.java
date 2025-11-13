package com.example.backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddMembersRequestDtoTest {

    @Test
    @DisplayName("Getter และ Setter สำหรับ memberIds ควรทำงานถูกต้อง")
    void testGettersAndSetters_MemberIds() {
        // --- Arrange ---
        AddMembersRequestDto dto = new AddMembersRequestDto();
        List<Integer> mockMemberIds = List.of(1, 2, 3);

        // --- Act ---
        dto.setMemberIds(mockMemberIds);

        // --- Assert ---
        assertEquals(mockMemberIds, dto.getMemberIds());
        assertEquals(3, dto.getMemberIds().size());
    }
}