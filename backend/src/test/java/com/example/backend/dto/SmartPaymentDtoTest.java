package com.example.backend.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock; // ⭐️ ต้อง Import
import org.mockito.MockitoAnnotations; // ⭐️ ต้อง Import

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmartPaymentDtoTest {

    // เราจะ Mock UserDto เพราะเราไม่สนว่า UserDto ทำงานยังไง
    // เราสนแค่ว่า SmartPaymentDto "เก็บ" มันได้หรือไม่
    @Mock
    private UserDto mockFromUser;

    @Mock
    private UserDto mockToUser;

    @BeforeEach
    void setUp() {
        // A. ใช้สำหรับ khởi tạo @Mock
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Constructor ควรตั้งค่าได้ถูกต้อง")
    void constructor_ShouldSetFieldsCorrectly() {
        // --- Arrange ---
        BigDecimal amount = new BigDecimal("100.00");

        // --- Act ---
        SmartPaymentDto dto = new SmartPaymentDto(mockFromUser, mockToUser, amount);

        // --- Assert ---
        assertEquals(mockFromUser, dto.getFromUser());
        assertEquals(mockToUser, dto.getToUser());
        assertEquals(amount, dto.getAmount());
    }

    @Test
    @DisplayName("Getter และ Setter ควรทำงานถูกต้อง")
    void testGettersAndSetters() {
        // --- Arrange ---
        SmartPaymentDto dto = new SmartPaymentDto(null, null, null);
        BigDecimal newAmount = new BigDecimal("200.00");

        // --- Act ---
        dto.setFromUser(mockFromUser);
        dto.setToUser(mockToUser);
        dto.setAmount(newAmount);

        // --- Assert ---
        assertEquals(mockFromUser, dto.getFromUser());
        assertEquals(mockToUser, dto.getToUser());
        assertEquals(newAmount, dto.getAmount());
    }
}