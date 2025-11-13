package com.example.backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberDtoTest {

    @Test
    @DisplayName("Constructor (userId, fullName) ควรตั้งค่าได้ถูกต้อง")
    void constructor_ShouldSetFieldsCorrectly() {
        // --- Arrange & Act ---
        // 1. เรียก Constructor ที่เราต้องการทดสอบ
        MemberDto dto = new MemberDto(1, "Test User");

        // --- Assert ---
        // 2. ตรวจสอบว่า Getter คืนค่าที่ถูกต้อง
        assertEquals(1, dto.getUserId());
        assertEquals("Test User", dto.getFullName());
    }

    @Test
    @DisplayName("Getter และ Setter ควรทำงานถูกต้อง")
    void testGettersAndSetters() {
        // --- Arrange ---
        // 1. สร้าง DTO ด้วย Constructor ก่อน
        MemberDto dto = new MemberDto(1, "Test User");

        // --- Act ---
        // 2. เรียกใช้ Setters เพื่อ "เปลี่ยน" ค่า
        dto.setUserId(2);
        dto.setFullName("Updated User");

        // --- Assert ---
        // 3. ตรวจสอบว่า Getter คืนค่าที่ "อัปเดต" แล้ว
        assertEquals(2, dto.getUserId());
        assertEquals("Updated User", dto.getFullName());
    }
}