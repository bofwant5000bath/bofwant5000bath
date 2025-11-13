package com.example.backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupWithMembersDtoTest {

    @Test
    @DisplayName("Getter และ Setter ควรทำงานถูกต้อง")
    void testGettersAndSetters() {
        // --- Arrange ---
        GroupWithMembersDto dto = new GroupWithMembersDto();

        // (เราไม่จำเป็นต้อง mock MemberDto จริงๆ ก็ได้ แค่สร้าง List เปล่าๆ ก็เทสได้)
        List<MemberDto> mockMembers = List.of(new MemberDto(1, "Test User"));

        // --- Act ---
        dto.setGroupId(10);
        dto.setGroupName("Test Group");
        dto.setPinned(true);
        dto.setMembers(mockMembers);

        // --- Assert ---
        assertEquals(10, dto.getGroupId());
        assertEquals("Test Group", dto.getGroupName());
        assertTrue(dto.isPinned()); // ⭐️ (สำหรับ boolean เราใช้ assertTrue)
        assertEquals(mockMembers, dto.getMembers());
        assertEquals(1, dto.getMembers().size());
    }
}