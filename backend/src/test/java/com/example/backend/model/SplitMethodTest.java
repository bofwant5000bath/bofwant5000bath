package com.example.backend.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SplitMethodTest {

    @Test
    void fromString_shouldReturnCorrectEnum_whenValueIsValid() {
        // Assert ว่าสามารถแปลง String เป็น enum ได้ถูกต้อง (ไม่สน case)
        assertEquals(SplitMethod.equal, SplitMethod.fromString("equal"));
        assertEquals(SplitMethod.unequal, SplitMethod.fromString("UNEQUAL"));
        assertEquals(SplitMethod.by_tag, SplitMethod.fromString("By_Tag"));
    }

    @Test
    void fromString_shouldThrowIllegalArgumentException_whenValueIsInvalid() {
        // Arrange
        String invalidValue = "invalid_method";

        // Act & Assert
        // ตรวจสอบว่าโปรแกรมโยน Exception เมื่อเจอค่าที่ไม่รู้จัก
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SplitMethod.fromString(invalidValue);
        });

        String expectedMessage = "Invalid split method: " + invalidValue;
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void fromString_shouldThrowIllegalArgumentException_whenValueIsNull() {
        // Act & Assert
        // ตรวจสอบว่าโปรแกรมโยน Exception เมื่อค่าเป็น null
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SplitMethod.fromString(null);
        });

        String expectedMessage = "Split method cannot be null";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}