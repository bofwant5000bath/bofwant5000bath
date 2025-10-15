package com.example.backend.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BillParticipantIdTest {

    @Test
    void testConstructorsAndGetters() {
        // Test no-arg constructor
        BillParticipantId id1 = new BillParticipantId();
        assertNull(id1.getBillId());
        assertNull(id1.getUserId());

        // Test args constructor
        BillParticipantId id2 = new BillParticipantId(100, 200);
        assertEquals(100, id2.getBillId());
        assertEquals(200, id2.getUserId());
    }

    @Test
    void testSetters() {
        // Arrange
        BillParticipantId id = new BillParticipantId();

        // Act
        id.setBillId(101);
        id.setUserId(202);

        // Assert
        assertEquals(101, id.getBillId());
        assertEquals(202, id.getUserId());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        BillParticipantId id1 = new BillParticipantId(1, 10);
        BillParticipantId id2 = new BillParticipantId(1, 10); // Same values as id1
        BillParticipantId id3 = new BillParticipantId(2, 10); // Different billId
        BillParticipantId id4 = new BillParticipantId(1, 20); // Different userId

        // Assert equals
        assertEquals(id1, id2, "IDs with same values should be equal.");
        assertNotEquals(id1, id3, "IDs with different billId should not be equal.");
        assertNotEquals(id1, id4, "IDs with different userId should not be equal.");
        assertEquals(id1, id1, "An ID should be equal to itself.");
        assertNotEquals(id1, null, "An ID should not be equal to null.");
        assertNotEquals(id1, new Object(), "An ID should not be equal to an object of a different class.");

        // Assert hashCode
        assertEquals(id1.hashCode(), id2.hashCode(), "HashCodes of equal objects must be same.");
        assertNotEquals(id1.hashCode(), id3.hashCode(), "HashCodes of non-equal objects should ideally be different.");
    }
}