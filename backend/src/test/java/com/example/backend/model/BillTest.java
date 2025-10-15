package com.example.backend.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BillTest {

    @Mock
    private Group mockGroup;
    @Mock
    private User mockPaidByUser;
    @Mock
    private SplitMethod mockSplitMethod; // Mock enum ที่อาจจะยังไม่มี
    @Mock
    private BillParticipant mockParticipant;

    @Test
    void testGettersAndSetters() {
        // Arrange
        Bill bill = new Bill();
        BigDecimal amount = new BigDecimal("5000.00");
        LocalDateTime now = LocalDateTime.now();
        List<BillParticipant> participants = Collections.singletonList(mockParticipant);

        // Act
        bill.setBillId(1);
        bill.setGroup(mockGroup);
        bill.setTitle("New Gadget");
        bill.setDescription("Latest model");
        bill.setAmount(amount);
        bill.setPaidByUser(mockPaidByUser);
        bill.setSplitMethod(mockSplitMethod);
        bill.setBillDate(now);
        bill.setCreatedAt(now);
        bill.setParticipants(participants);

        // Assert
        assertEquals(1, bill.getBillId());
        assertEquals(mockGroup, bill.getGroup());
        assertEquals("New Gadget", bill.getTitle());
        assertEquals("Latest model", bill.getDescription());
        assertEquals(0, amount.compareTo(bill.getAmount()));
        assertEquals(mockPaidByUser, bill.getPaidByUser());
        assertEquals(mockSplitMethod, bill.getSplitMethod());
        assertEquals(now, bill.getBillDate());
        assertEquals(now, bill.getCreatedAt());
        assertEquals(participants, bill.getParticipants());
        assertEquals(1, bill.getParticipants().size());
    }
}