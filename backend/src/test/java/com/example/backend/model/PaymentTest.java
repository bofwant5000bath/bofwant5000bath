package com.example.backend.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentTest {

    @Mock
    private Bill mockBill; // "นักแสดงแทน" ของ Bill
    @Mock
    private User mockUser; // "นักแสดงแทน" ของ User

    @Test
    void testGettersAndSetters() {
        // Arrange
        Payment payment = new Payment();
        BigDecimal amount = new BigDecimal("999.99");
        LocalDateTime paymentDate = LocalDateTime.now();

        // Act
        payment.setPaymentId(1);
        payment.setBill(mockBill);
        payment.setPayerUser(mockUser);
        payment.setAmount(amount);
        payment.setPaymentDate(paymentDate);

        // Assert
        assertEquals(1, payment.getPaymentId());
        assertEquals(mockBill, payment.getBill());
        assertEquals(mockUser, payment.getPayerUser());
        assertEquals(0, amount.compareTo(payment.getAmount()));
        assertEquals(paymentDate, payment.getPaymentDate());
    }

    @Test
    void testDefaultPaymentDate() {
        // Arrange & Act
        Payment payment = new Payment();

        // Assert
        // ตรวจสอบว่า paymentDate ถูกกำหนดค่า default เป็นเวลาปัจจุบันตอนสร้าง object
        assertNotNull(payment.getPaymentDate());
    }
}