package com.example.backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PinnedGroupIdTest {

    @Test
    @DisplayName("Constructor (userId, groupId) ควรตั้งค่าได้ถูกต้อง")
    void constructor_ShouldSetFieldsCorrectly() {
        // --- Arrange & Act ---
        PinnedGroupId id = new PinnedGroupId(1, 10);

        // --- Assert ---
        assertEquals(1, id.getUserId());
        assertEquals(10, id.getGroupId());
    }

    @Test
    @DisplayName("Constructor (no-arg) ควรสร้าง object ได้")
    void noArgConstructor_ShouldCreateInstance() {
        // --- Arrange & Act ---
        PinnedGroupId id = new PinnedGroupId();

        // --- Assert ---
        assertNull(id.getUserId());
        assertNull(id.getGroupId());
    }

    @Test
    @DisplayName("Getter และ Setter ควรทำงานถูกต้อง")
    void testGettersAndSetters() {
        // --- Arrange ---
        PinnedGroupId id = new PinnedGroupId();

        // --- Act ---
        id.setUserId(2);
        id.setGroupId(20);

        // --- Assert ---
        assertEquals(2, id.getUserId());
        assertEquals(20, id.getGroupId());
    }

    @Test
    @DisplayName("equals() ควรเปรียบเทียบค่าได้ถูกต้อง")
    void equals_ShouldCompareValuesCorrectly() {
        // --- Arrange ---
        PinnedGroupId id1 = new PinnedGroupId(1, 10);
        PinnedGroupId id2 = new PinnedGroupId(1, 10); // ค่าเหมือน id1
        PinnedGroupId id3 = new PinnedGroupId(2, 10); // userId ต่าง
        PinnedGroupId id4 = new PinnedGroupId(1, 20); // groupId ต่าง

        // --- Assert ---
        // 1. เปรียบเทียบกับตัวเอง
        assertTrue(id1.equals(id1));

        // 2. เปรียบเทียบกับ object ที่ค่าเหมือนกัน
        assertTrue(id1.equals(id2));
        assertTrue(id2.equals(id1));

        // 3. เปรียบเทียบกับ object ที่ค่าต่างกัน
        assertFalse(id1.equals(id3));
        assertFalse(id1.equals(id4));

        // 4. เปรียบเทียบกับ null
        assertFalse(id1.equals(null));

        // 5. เปรียบเทียบกับคลาสอื่น
        assertFalse(id1.equals(new Object()));
    }

    @Test
    @DisplayName("hashCode() ควรคืนค่าเท่ากันสำหรับ object ที่เท่ากัน")
    void hashCode_ShouldBeConsistent() {
        // --- Arrange ---
        PinnedGroupId id1 = new PinnedGroupId(1, 10);
        PinnedGroupId id2 = new PinnedGroupId(1, 10);

        // --- Assert ---
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}