package com.example.backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PinGroupRequestDtoTest {

    @Test
    @DisplayName("Getter และ Setter ควรทำงานถูกต้อง")
    void testGettersAndSetters() {
        // --- Arrange ---
        PinGroupRequestDto dto = new PinGroupRequestDto();

        // --- Act ---
        dto.setUserId(1);
        dto.setGroupId(10);
        dto.setPin(true);

        // --- Assert ---
        assertEquals(1, dto.getUserId());
        assertEquals(10, dto.getGroupId());
        assertTrue(dto.isPin()); // ⭐️ (ใช้ .isPin() สำหรับ boolean)
    }

    @Test
    @DisplayName("Getter และ Setter ควรทำงานถูกต้องสำหรับค่า False")
    void testGettersAndSetters_ForFalse() {
        // --- Arrange ---
        PinGroupRequestDto dto = new PinGroupRequestDto();

        // --- Act ---
        dto.setPin(false);

        // --- Assert ---
        assertFalse(dto.isPin()); // ⭐️ (ใช้ assertFalse)
    }
}