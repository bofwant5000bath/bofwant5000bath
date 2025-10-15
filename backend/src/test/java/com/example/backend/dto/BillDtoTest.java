package com.example.backend.dto;

import com.example.backend.model.Bill;
import com.example.backend.model.SplitMethod;
import com.example.backend.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillDtoTest {

    @Mock
    private Bill mockBill;
    @Mock
    private User mockPaidByUser;
    @Mock
    private SplitMethod mockSplitMethod;

    @Test
    void constructor_ShouldCorrectlyMapBillToDto() {
        // Arrange: กำหนดบทบาทให้นักแสดงแทน (Mocks)
        when(mockBill.getBillId()).thenReturn(101);
        when(mockBill.getTitle()).thenReturn("Dinner with Friends");
        when(mockBill.getDescription()).thenReturn("Italian restaurant");
        when(mockBill.getAmount()).thenReturn(new BigDecimal("2500.75"));
        when(mockBill.getBillDate()).thenReturn(LocalDateTime.now());

        // กำหนดการทำงานของ Mock ที่ซ้อนกัน
        when(mockBill.getPaidByUser()).thenReturn(mockPaidByUser);
        when(mockPaidByUser.getUserId()).thenReturn(1);
        when(mockPaidByUser.getUsername()).thenReturn("test_user");

        when(mockBill.getSplitMethod()).thenReturn(mockSplitMethod);
        when(mockSplitMethod.toString()).thenReturn("EQUAL");

        // Act: สร้าง DTO จากนักแสดงแทน
        BillDto billDto = new BillDto(mockBill);

        // Assert: ตรวจสอบว่า DTO ได้รับข้อมูลมาถูกต้อง
        assertNotNull(billDto);
        assertEquals(101, billDto.getBillId());
        assertEquals("Dinner with Friends", billDto.getTitle());
        assertEquals("Italian restaurant", billDto.getDescription());
        assertEquals(0, new BigDecimal("2500.75").compareTo(billDto.getAmount())); // ใช้ compareTo สำหรับ BigDecimal
        assertEquals("EQUAL", billDto.getSplitMethod());
        assertNotNull(billDto.getBillDate());

        // ตรวจสอบข้อมูล UserDto ที่ถูกสร้างขึ้นภายใน
        assertNotNull(billDto.getPaidByUser());
        assertEquals(1, billDto.getPaidByUser().getUserId());
        assertEquals("test_user", billDto.getPaidByUser().getUsername());
    }
}