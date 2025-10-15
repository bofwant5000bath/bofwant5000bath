package com.example.backend.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CreatePaymentRequestTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        BigDecimal paymentAmount = new BigDecimal("150.75");

        // Act
        request.setBillId(101);
        request.setPayerUserId(2);
        request.setAmount(paymentAmount);

        // Assert
        assertEquals(101, request.getBillId());
        assertEquals(2, request.getPayerUserId());
        assertEquals(0, paymentAmount.compareTo(request.getAmount())); // Use compareTo for BigDecimal
    }
}