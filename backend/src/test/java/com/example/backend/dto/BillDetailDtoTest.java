package com.example.backend.dto;

import com.example.backend.model.Bill;
import com.example.backend.model.BillParticipant;
import com.example.backend.model.SplitMethod;
import com.example.backend.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillDetailDtoTest {

    @Mock
    private Bill mockBill;
    @Mock
    private User mockUser;
    @Mock
    private BillParticipant mockParticipant;
    @Mock
    private SplitMethod mockSplitMethod;

    @Test
    void testConstructor_ShouldMapDataFromMocksCorrectly() {
        // Arrange: 1. กำหนดบทบาทให้นักแสดงแทน
        when(mockBill.getSplitMethod()).thenReturn(mockSplitMethod);
        when(mockSplitMethod.toString()).thenReturn("EQUAL");

        when(mockBill.getBillId()).thenReturn(202);
        when(mockBill.getTitle()).thenReturn("Movie Night");
        when(mockBill.getAmount()).thenReturn(new BigDecimal("500.00"));
        when(mockBill.getBillDate()).thenReturn(LocalDateTime.now());
        when(mockBill.getPaidByUser()).thenReturn(mockUser);

        when(mockUser.getUserId()).thenReturn(1);
        when(mockUser.getUsername()).thenReturn("payer");

        // 👇 *** บรรทัดที่เพิ่มเข้ามาเพื่อแก้ปัญหา *** 👇
        // สั่งให้ mockParticipant คืนค่า mockUser เมื่อถูกเรียก .getUser()
        when(mockParticipant.getUser()).thenReturn(mockUser);

        List<BillParticipant> mockParticipantList = Collections.singletonList(mockParticipant);

        // Act: 2. เรียกใช้งาน DTO โดยส่ง "นักแสดงแทน" เข้าไป
        BillDetailDto billDetailDto = new BillDetailDto(mockBill, mockParticipantList);

        // Assert: 3. ตรวจสอบว่า DTO ได้รับข้อมูลจากนักแสดงแทนถูกต้อง
        assertNotNull(billDetailDto);
        assertEquals(202, billDetailDto.getBillId());
        assertEquals("EQUAL", billDetailDto.getSplitMethod());
        assertEquals(1, billDetailDto.getPaidByUser().getUserId());

        // ตรวจสอบข้อมูลใน list participant
        assertNotNull(billDetailDto.getParticipants());
        assertEquals(1, billDetailDto.getParticipants().size());
        assertEquals(1, billDetailDto.getParticipants().get(0).getUser().getUserId());
    }
}