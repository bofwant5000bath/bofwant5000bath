package com.example.backend.dto;

import com.example.backend.model.BillParticipant;
import com.example.backend.model.PaymentStatus;
import com.example.backend.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillParticipantDtoTest {

    @Mock
    private BillParticipant mockBillParticipant;
    @Mock
    private User mockParticipantUser;
    @Mock
    private PaymentStatus mockPaymentStatus; // Mock enum PaymentStatus

    @Test
    void constructor_ShouldCorrectlyMapParticipantToDto() {
        // Arrange: กำหนดบทบาทให้นักแสดงแทน (Mocks)
        when(mockBillParticipant.getSplitAmount()).thenReturn(new BigDecimal("500.25"));

        // กำหนดการทำงานของ Mock ที่ซ้อนกัน
        when(mockBillParticipant.getUser()).thenReturn(mockParticipantUser);
        when(mockParticipantUser.getUserId()).thenReturn(2);
        when(mockParticipantUser.getUsername()).thenReturn("participant_1");

        // ให้ Mock คืนค่า enum ที่เป็น Mock เช่นกัน
        when(mockBillParticipant.getIsPaid()).thenReturn(mockPaymentStatus);


        // Act: สร้าง DTO จากนักแสดงแทน
        BillParticipantDto participantDto = new BillParticipantDto(mockBillParticipant);

        // Assert: ตรวจสอบว่า DTO ได้รับข้อมูลมาถูกต้อง
        assertNotNull(participantDto);
        assertEquals(0, new BigDecimal("500.25").compareTo(participantDto.getSplitAmount()));
        assertEquals(mockPaymentStatus, participantDto.getStatus()); // ตรวจสอบว่าได้รับ enum object ที่ถูกต้อง

        // ตรวจสอบข้อมูล UserDto ที่ถูกสร้างขึ้นภายใน
        assertNotNull(participantDto.getUser());
        assertEquals(2, participantDto.getUser().getUserId());
        assertEquals("participant_1", participantDto.getUser().getUsername());
    }
}