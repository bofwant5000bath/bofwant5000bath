package com.example.backend.dto;

import com.example.backend.model.Bill;
import com.example.backend.model.Payment;
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
class PaymentDtoTest {

    @Mock
    private Payment mockPayment;
    @Mock
    private Bill mockBill;
    @Mock
    private User mockPayerUser;

    @Test
    void constructor_ShouldMapAllFieldsFromPayment() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        when(mockPayment.getPaymentId()).thenReturn(501);
        when(mockPayment.getAmount()).thenReturn(new BigDecimal("250.00"));
        when(mockPayment.getPaymentDate()).thenReturn(now);

        // Mock การเชื่อมต่อไปยัง Bill
        when(mockPayment.getBill()).thenReturn(mockBill);
        when(mockBill.getBillId()).thenReturn(101);

        // Mock การเชื่อมต่อไปยัง User
        when(mockPayment.getPayerUser()).thenReturn(mockPayerUser);
        when(mockPayerUser.getUserId()).thenReturn(2);
        when(mockPayerUser.getUsername()).thenReturn("jane.doe");

        // Act
        PaymentDto paymentDto = new PaymentDto(mockPayment);

        // Assert
        assertEquals(501, paymentDto.getPaymentId());
        assertEquals(101, paymentDto.getBillId());
        assertEquals(0, new BigDecimal("250.00").compareTo(paymentDto.getAmount()));
        assertEquals(now, paymentDto.getPaymentDate());

        assertNotNull(paymentDto.getPayerUser());
        assertEquals(2, paymentDto.getPayerUser().getUserId());
        assertEquals("jane.doe", paymentDto.getPayerUser().getUsername());
    }
}