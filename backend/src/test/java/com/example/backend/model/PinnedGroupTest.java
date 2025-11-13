package com.example.backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PinnedGroupTest {

    // เราจะ Mock Model อื่นๆ ที่ PinnedGroup ต้องใช้
    @Mock
    private User mockUser;

    @Mock
    private Group mockGroup;

    @Mock
    private PinnedGroupId mockId;

    @BeforeEach
    void setUp() {
        // A. ใช้สำหรับ khởi tạo @Mock
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Constructor (User, Group) ควรสร้าง ID และตั้งค่า Default ได้ถูกต้อง")
    void constructor_UserAndGroup_ShouldCreateIdAndSetDefaults() {
        // --- Arrange ---
        // 1. ตั้งค่า Mock User และ Group ให้คืนค่า ID
        when(mockUser.getUserId()).thenReturn(1);
        when(mockGroup.getGroupId()).thenReturn(10);

        // --- Act ---
        // 2. เรียก Constructor ที่เราต้องการทดสอบ
        PinnedGroup pinnedGroup = new PinnedGroup(mockUser, mockGroup);

        // --- Assert ---
        // 3. ตรวจสอบว่า User/Group ถูก set
        assertEquals(mockUser, pinnedGroup.getUser());
        assertEquals(mockGroup, pinnedGroup.getGroup());

        // 4. ตรวจสอบว่า isPinned เป็น true (ตาม Logic)
        assertTrue(pinnedGroup.getPinned());

        // 5. ⭐️ ตรวจสอบว่า ID ถูกสร้างขึ้นอย่างถูกต้อง (ตาม Logic)
        assertNotNull(pinnedGroup.getId());
        assertEquals(1, pinnedGroup.getId().getUserId());
        assertEquals(10, pinnedGroup.getId().getGroupId());
    }

    @Test
    @DisplayName("Getter และ Setter ควรทำงานถูกต้อง")
    void testGettersAndSetters() {
        // --- Arrange ---
        PinnedGroup pinnedGroup = new PinnedGroup();

        // --- Act ---
        pinnedGroup.setId(mockId);
        pinnedGroup.setUser(mockUser);
        pinnedGroup.setGroup(mockGroup);
        pinnedGroup.setPinned(false); // ⭐️ (ทดสอบการ set ค่าอื่น)

        // --- Assert ---
        assertEquals(mockId, pinnedGroup.getId());
        assertEquals(mockUser, pinnedGroup.getUser());
        assertEquals(mockGroup, pinnedGroup.getGroup());
        assertFalse(pinnedGroup.getPinned());
    }

    @Test
    @DisplayName("Constructor (no-arg) ควรสร้าง object ได้")
    void noArgConstructor_ShouldCreateInstance() {
        // --- Arrange & Act ---
        PinnedGroup pinnedGroup = new PinnedGroup();

        // --- Assert ---
        assertNull(pinnedGroup.getId());
        assertNull(pinnedGroup.getUser());
        assertNull(pinnedGroup.getGroup());
        // (Boolean isPinned อาจจะมี default value ในคลาส)
    }
}