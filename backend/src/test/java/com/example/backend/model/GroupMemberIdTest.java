package com.example.backend.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GroupMemberIdTest {

    @Test
    void testConstructorAndGettersSetters() {
        // Arrange
        GroupMemberId id = new GroupMemberId();

        // Assert no-arg constructor
        assertNull(id.getGroup());
        assertNull(id.getUser());

        // Act
        id.setGroup(10);
        id.setUser(20);

        // Assert setters
        assertEquals(10, id.getGroup());
        assertEquals(20, id.getUser());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        GroupMemberId id1 = new GroupMemberId();
        id1.setGroup(1);
        id1.setUser(100);

        GroupMemberId id2 = new GroupMemberId();
        id2.setGroup(1);
        id2.setUser(100); // Same values as id1

        GroupMemberId id3 = new GroupMemberId();
        id3.setGroup(2); // Different group
        id3.setUser(100);

        GroupMemberId id4 = new GroupMemberId();
        id4.setGroup(1);
        id4.setUser(200); // Different user

        // Assert equals
        assertEquals(id1, id2, "IDs with same values should be equal.");
        assertNotEquals(id1, id3, "IDs with different group IDs should not be equal.");
        assertNotEquals(id1, id4, "IDs with different user IDs should not be equal.");
        assertEquals(id1, id1, "An object should be equal to itself.");
        assertNotEquals(null, id1, "An object should not be equal to null.");
        assertNotEquals(id1, new Object(), "An object should not be equal to an object of another class.");


        // Assert hashCode
        assertEquals(id1.hashCode(), id2.hashCode(), "HashCodes of equal objects must be the same.");
    }
}