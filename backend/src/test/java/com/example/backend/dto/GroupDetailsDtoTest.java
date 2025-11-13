package com.example.backend.dto;

import com.example.backend.model.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class GroupDetailsDtoTest {

    @Mock
    private Group mockGroup; // เราจะ Mock Group (Model)

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // A. ตั้งค่าพื้นฐานให้ mockGroup ที่จะใช้ในทุกเทส
        when(mockGroup.getGroupId()).thenReturn(1);
        when(mockGroup.getGroupName()).thenReturn("Test Group");
    }

    @Test
    @DisplayName("Constructor ควรแมปค่าทั้งหมด เมื่อค่าไม่เป็น null (Happy Path)")
    void constructor_ShouldMapAllValues_WhenNotNull() {
        // --- Arrange ---
        Integer memberCount = 5;
        BigDecimal totalAmount = new BigDecimal("1000.00");
        BigDecimal myDebt = new BigDecimal("100.00");
        BigDecimal othersDebt = new BigDecimal("50.00");
        boolean isPinned = true;

        // --- Act ---
        GroupDetailsDto dto = new GroupDetailsDto(
                mockGroup, memberCount, totalAmount, myDebt, othersDebt, isPinned
        );

        // --- Assert ---
        assertEquals(1, dto.getGroupId());
        assertEquals("Test Group", dto.getGroupName());
        assertEquals(5, dto.getMemberCount());
        assertEquals(0, new BigDecimal("1000.00").compareTo(dto.getGroupTotalAmount()));
        assertEquals(0, new BigDecimal("100.00").compareTo(dto.getMyDebt()));
        assertEquals(0, new BigDecimal("50.00").compareTo(dto.getOthersDebtToMe()));
        assertTrue(dto.isPinned());
    }

    @Test
    @DisplayName("Constructor ควรใช้ค่า Default (0) เมื่อค่าเป็น null")
    void constructor_ShouldUseDefaultValues_WhenNull() {
        // --- Arrange ---
        // ⭐️⭐️⭐️ นี่คือส่วนที่สำคัญที่สุด ⭐️⭐️⭐️
        // เราจงใจส่งค่า null เข้าไปใน Constructor
        Integer memberCount = null;
        BigDecimal totalAmount = null;
        BigDecimal myDebt = null;
        BigDecimal othersDebt = null;
        boolean isPinned = false;

        // --- Act ---
        GroupDetailsDto dto = new GroupDetailsDto(
                mockGroup, memberCount, totalAmount, myDebt, othersDebt, isPinned
        );

        // --- Assert ---
        // ⭐️⭐️⭐️ ตรวจสอบว่า DTO ใช้ค่า Default (0) แทน null ⭐️⭐️⭐️
        assertEquals(0, dto.getMemberCount());
        assertEquals(BigDecimal.ZERO, dto.getGroupTotalAmount());
        assertEquals(BigDecimal.ZERO, dto.getMyDebt());
        assertEquals(BigDecimal.ZERO, dto.getOthersDebtToMe());
    }
}