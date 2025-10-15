package com.example.backend.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentStatusTest {

    @Test
    void testEnumValues() {
        // Assert ว่า enum constants มีอยู่จริง
        assertNotNull(PaymentStatus.valueOf("unpaid"));
        assertNotNull(PaymentStatus.valueOf("partial"));
        assertNotNull(PaymentStatus.valueOf("paid"));
    }

    @Test
    void testAllEnumValuesArePresent() {
        // Assert ว่าจำนวนค่าใน enum ตรงกับที่คาดหวัง
        PaymentStatus[] statuses = PaymentStatus.values();
        assertEquals(3, statuses.length);
        assertEquals(PaymentStatus.unpaid, statuses[0]);
        assertEquals(PaymentStatus.partial, statuses[1]);
        assertEquals(PaymentStatus.paid, statuses[2]);
    }
}