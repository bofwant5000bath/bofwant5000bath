package com.example.backend.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BillParticipantTest {

    @Mock
    private BillParticipantId mockId;
    @Mock
    private Bill mockBill;
    @Mock
    private User mockUser;
    @Mock
    private PaymentStatus mockPaymentStatus; // Mock enum ที่อาจจะยังไม่มี

    @Test
    void testGettersAndSetters() {
        // Arrange
        BillParticipant participant = new BillParticipant();
        BigDecimal splitAmount = new BigDecimal("150.50");

        // Act
        participant.setId(mockId);
        participant.setBill(mockBill);
        participant.setUser(mockUser);
        participant.setSplitAmount(splitAmount);
        participant.setIsPaid(mockPaymentStatus);

        // Assert
        assertEquals(mockId, participant.getId());
        assertEquals(mockBill, participant.getBill());
        assertEquals(mockUser, participant.getUser());
        assertEquals(0, splitAmount.compareTo(participant.getSplitAmount()));
        assertEquals(mockPaymentStatus, participant.getIsPaid());
    }
}