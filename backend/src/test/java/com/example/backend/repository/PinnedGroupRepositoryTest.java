package com.example.backend.repository;

import com.example.backend.model.Group;
import com.example.backend.model.PinnedGroup;
import com.example.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat; // ⭐️ (ใช้ AssertJ จะเทส List ได้ง่ายกว่า)

// 1. ระบุว่าเป็นการทดสอบ JPA Layer (จะใช้ H2 In-memory DB)
@DataJpaTest
class PinnedGroupRepositoryTest {

    // 2. ตัวจัดการ Entity สำหรับยัดข้อมูลลง DB (Arrange)
    @Autowired
    private TestEntityManager entityManager;

    // 3. Repository จริง (ที่เราจะทดสอบ)
    @Autowired
    private PinnedGroupRepository pinnedGroupRepository;

    //--- ตัวแปรกลางสำหรับเก็บ Entity ที่ persist แล้ว ---
    private User user1, user2;
    private Group group1, group2;

    @BeforeEach
    void setUp() {
        // --- Arrange (ข้อมูลตั้งต้น) ---
        // (เราต้องสร้าง User และ Group ก่อน เพราะ PinnedGroup อ้างอิงถึง)

        user1 = new User();
        user1.setUsername("user1");
        user1.setFullName("User One");
        user1.setPassword("pass123");
        entityManager.persist(user1); // บันทึกลง H2

        user2 = new User();
        user2.setUsername("user2");
        user2.setFullName("User Two");
        user2.setPassword("pass123");
        entityManager.persist(user2);

        group1 = new Group();
        group1.setGroupName("Group 1");
        group1.setCreatedByUser(user1); // (สมมติว่า Group ต้องมี createdBy)
        entityManager.persist(group1);

        group2 = new Group();
        group2.setGroupName("Group 2");
        group2.setCreatedByUser(user2);
        entityManager.persist(group2);

        // (เรา flush เพื่อเคลียร์ context ก่อนเริ่มเทสจริง)
        entityManager.flush();
    }

    @Test
    @DisplayName("findByIdUserId ควรคืนรายการที่ปักหมุดสำหรับ User ที่ระบุเท่านั้น")
    void findByIdUserId_ShouldReturnPinsForCorrectUser() {
        // --- Arrange ---
        // 1. User 1 ปักหมุด Group 1
        PinnedGroup pin1 = new PinnedGroup(user1, group1);
        entityManager.persist(pin1);

        // 2. User 1 ปักหมุด Group 2
        PinnedGroup pin2 = new PinnedGroup(user1, group2);
        entityManager.persist(pin2);

        // 3. User 2 ปักหมุด Group 1 (ข้อมูลนี้ต้องไม่ถูกดึงมา)
        PinnedGroup pin3_user2 = new PinnedGroup(user2, group1);
        entityManager.persist(pin3_user2);

        entityManager.flush(); // ⭐️ (สำคัญ) ยืนยันข้อมูลลง DB ก่อน Query

        // --- Act ---
        // 4. เรียกเมธอดที่เราต้องการทดสอบ (หา Pin ของ User 1)
        List<PinnedGroup> results = pinnedGroupRepository.findByIdUserId(user1.getUserId());

        // --- Assert ---
        // 5. ตรวจสอบว่าได้ผลลัพธ์ 2 รายการ
        assertThat(results).hasSize(2);
        // 6. ตรวจสอบว่าได้ pin1 และ pin2 (และไม่มี pin3)
        assertThat(results).containsExactlyInAnyOrder(pin1, pin2);
    }

    @Test
    @DisplayName("findByIdUserId ควรคืน List ว่าง ถ้า User ไม่ได้ปักหมุดอะไรเลย")
    void findByIdUserId_ShouldReturnEmptyList_WhenUserHasNoPins() {
        // --- Arrange ---
        // (User 2 ปักหมุด แต่ User 1 ไม่ได้ปักหมุด)
        PinnedGroup pin_user2 = new PinnedGroup(user2, group1);
        entityManager.persist(pin_user2);
        entityManager.flush();

        // --- Act ---
        // (เรียกหา Pin ของ User 1)
        List<PinnedGroup> results = pinnedGroupRepository.findByIdUserId(user1.getUserId());

        // --- Assert ---
        // (ผลลัพธ์ต้องเป็น List ว่าง)
        assertThat(results).isEmpty();
    }
}