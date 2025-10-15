package com.example.backend.dto;

import com.example.backend.model.Group;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupDetailsDtoTest {

    @Mock
    private Group mockGroup;

    @Test
    void constructor_ShouldMapAllValuesWhenNotNull() {
        // Arrange
        when(mockGroup.getGroupId()).thenReturn(1);
        when(mockGroup.getGroupName()).thenReturn("Test Group");
        Integer memberCount = 5;
        BigDecimal totalAmount = new BigDecimal("1000.00");
        BigDecimal myDebt = new BigDecimal("100.00");
        BigDecimal othersDebt = new BigDecimal("50.00");

        // Act
        GroupDetailsDto dto = new GroupDetailsDto(mockGroup, memberCount, totalAmount, myDebt, othersDebt);

        // Assert
        assertEquals(1, dto.getGroupId());
        assertEquals("Test Group", dto.getGroupName());
        assertEquals(5, dto.getMemberCount());
        assertEquals(0, totalAmount.compareTo(dto.getGroupTotalAmount()));
        assertEquals(0, myDebt.compareTo(dto.getMyDebt()));
        assertEquals(0, othersDebt.compareTo(dto.getOthersDebtToMe()));
    }

    @Test
    void constructor_ShouldSetDefaultValuesWhenNumericInputsAreNull() {
        // Arrange
        when(mockGroup.getGroupId()).thenReturn(2);
        when(mockGroup.getGroupName()).thenReturn("Empty Group");

        // Act: ส่งค่า null เข้าไปใน constructor
        GroupDetailsDto dto = new GroupDetailsDto(mockGroup, null, null, null, null);

        // Assert: ตรวจสอบว่าค่าถูกเปลี่ยนเป็น 0 หรือ BigDecimal.ZERO
        assertEquals(2, dto.getGroupId());
        assertEquals("Empty Group", dto.getGroupName());
        assertEquals(0, dto.getMemberCount());
        assertEquals(BigDecimal.ZERO, dto.getGroupTotalAmount());
        assertEquals(BigDecimal.ZERO, dto.getMyDebt());
        assertEquals(BigDecimal.ZERO, dto.getOthersDebtToMe());
    }
}