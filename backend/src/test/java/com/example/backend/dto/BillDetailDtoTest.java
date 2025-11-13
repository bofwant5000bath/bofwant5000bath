package com.example.backend.dto;

import com.example.backend.model.Bill;
import com.example.backend.model.BillParticipant;
import com.example.backend.model.SplitMethod;
import com.example.backend.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BillDetailDtoTest {

    @Test
    @DisplayName("Constructor ควรแมปค่า คำนวณ และแปลง List ได้ถูกต้อง")
    void constructor_ShouldMapCalculateAndTransformCorrectly() {
        // --- Arrange ---
        // 1. สร้าง User (Payer และ Participant) จำลอง
        User mockUser = mock(User.class);
        when(mockUser.getFullName()).thenReturn("Test User");

        // 2. สร้าง Bill จำลอง (ตัวป้อนหลัก)
        Bill mockBill = mock(Bill.class);
        when(mockBill.getBillId()).thenReturn(1);
        when(mockBill.getTitle()).thenReturn("Test Bill");
        when(mockBill.getDescription()).thenReturn("Test Desc");
        when(mockBill.getPromptpayNumber()).thenReturn("0801234567");
        when(mockBill.getReceiptImageUrl()).thenReturn("http://image.com/img.jpg");
        when(mockBill.getCurrencyCode()).thenReturn("USD");
        when(mockBill.getPaidByUser()).thenReturn(mockUser);

        // (ผมแก้ 'equal' เป็น SplitMethod.equal ให้ตรงกับโค้ดครั้งที่แล้วของคุณ)
        when(mockBill.getSplitMethod()).thenReturn(SplitMethod.equal);

        when(mockBill.getBillDate()).thenReturn(LocalDateTime.now());

        // นี่คือจุดที่ใช้ในการคำนวณ
        when(mockBill.getAmount()).thenReturn(new BigDecimal("100.00")); // scale 2
        when(mockBill.getExchangeRate()).thenReturn(new BigDecimal("1.50")); // scale 2

        // 3. สร้าง List<BillParticipant> จำลอง (ตัวป้อนที่สอง)
        BillParticipant mockParticipant = mock(BillParticipant.class);
        when(mockParticipant.getUser()).thenReturn(mockUser);

        List<BillParticipant> mockParticipantsList = List.of(mockParticipant);

        // --- Act ---
        // 4. เรียก Constructor ที่เราต้องการทดสอบ
        BillDetailDto dto = new BillDetailDto(mockBill, mockParticipantsList);

        // --- Assert ---
        // 5. ตรวจสอบการแมปค่าพื้นฐาน
        assertEquals(1, dto.getBillId());
        assertEquals("Test Bill", dto.getTitle());
        assertEquals("USD", dto.getCurrencyCode());
        assertEquals("0801234567", dto.getPromptpayNumber());
        assertEquals("http://image.com/img.jpg", dto.getReceiptImageUrl());
        assertEquals("equal", dto.getSplitMethod());

        // 6. ⭐️ ตรวจสอบการคำนวณ (สำคัญ)
        // ⭐️⭐️⭐️ FIX (บรรทัด 67) ⭐️⭐️⭐️
        // (100.00 * 1.50) = 150.0000
        // เราใช้ compareTo() เพื่อเปรียบเทียบค่า โดยไม่สน scale
        assertEquals(0, new BigDecimal("150.00").compareTo(dto.getAmountInThb()));

        // 7. ⭐️ ตรวจสอบการแปลง List (สำคัญ)
        assertEquals(1, dto.getParticipants().size());
        assertEquals("Test User", dto.getParticipants().get(0).getUser().getFullName());
    }
}