package com.example.backend.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DashboardSummaryDtoTest {

    @Test
    void testGettersAndSettersAndConstructor() {
        // Arrange
        BigDecimal totalOwed = new BigDecimal("250.50");
        BigDecimal totalReceivable = new BigDecimal("500.00");
        List<GroupDetailsDto> groups = new ArrayList<>(); // ใช้ List ว่างๆ ก็ได้

        // Act
        DashboardSummaryDto summaryDto = new DashboardSummaryDto(totalOwed, totalReceivable, groups);

        // Assert Constructor
        assertEquals(0, totalOwed.compareTo(summaryDto.getTotalOwed()));
        assertEquals(0, totalReceivable.compareTo(summaryDto.getTotalReceivable()));
        assertEquals(groups, summaryDto.getGroups());

        // Assert Setters
        BigDecimal newOwed = new BigDecimal("300.00");
        summaryDto.setTotalOwed(newOwed);
        assertEquals(0, newOwed.compareTo(summaryDto.getTotalOwed()));
    }
}