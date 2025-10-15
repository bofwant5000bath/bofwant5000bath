package com.example.backend.repository;

import com.example.backend.model.Bill;
import com.example.backend.model.Group;
import com.example.backend.model.User;
import com.example.backend.model.SplitMethod; // สมมติว่ามี enum นี้อยู่
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // 👈 Annotation สำคัญสำหรับเทส Repository
class BillRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // 👈 ตัวช่วยสำหรับจัดการ Entity ในการทดสอบ

    @Autowired
    private BillRepository billRepository;

    private Group group1;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Arrange: สร้างข้อมูลตั้งต้นสำหรับทุกเทส
        group1 = new Group();
        group1.setGroupName("Trip to Japan");
        entityManager.persist(group1);

        user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("pass");
        user1.setFullName("User One");
        entityManager.persist(user1);

        user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pass");
        user2.setFullName("User Two");
        entityManager.persist(user2);

        // Bill 1: จ่ายโดย user1
        Bill bill1 = new Bill();
        bill1.setGroup(group1);
        bill1.setPaidByUser(user1);
        bill1.setAmount(new BigDecimal("100.00"));
        bill1.setTitle("Lunch");
        bill1.setSplitMethod(SplitMethod.equal); // สมมติว่า enum ชื่อ equal
        bill1.setBillDate(LocalDateTime.now());
        entityManager.persist(bill1);

        // Bill 2: จ่ายโดย user1
        Bill bill2 = new Bill();
        bill2.setGroup(group1);
        bill2.setPaidByUser(user1);
        bill2.setAmount(new BigDecimal("50.50"));
        bill2.setTitle("Coffee");
        bill2.setSplitMethod(SplitMethod.equal);
        bill2.setBillDate(LocalDateTime.now());
        entityManager.persist(bill2);

        // Bill 3: จ่ายโดย user2
        Bill bill3 = new Bill();
        bill3.setGroup(group1);
        bill3.setPaidByUser(user2);
        bill3.setAmount(new BigDecimal("200.00"));
        bill3.setTitle("Tickets");
        bill3.setSplitMethod(SplitMethod.equal);
        bill3.setBillDate(LocalDateTime.now());
        entityManager.persist(bill3);

        entityManager.flush(); // บันทึกข้อมูลทั้งหมดลง DB จำลอง
    }

    @Test
    void findByGroupGroupId_shouldReturnAllBillsInGroup() {
        // Act
        List<Bill> bills = billRepository.findByGroupGroupId(group1.getGroupId());

        // Assert
        assertEquals(3, bills.size(), "ควรจะเจอ 3 บิลในกลุ่มนี้");
    }

    @Test
    void sumAmountByGroupId_shouldReturnCorrectTotalAmount() {
        // Act
        BigDecimal sum = billRepository.sumAmountByGroupId(group1.getGroupId());

        // Assert: 100.00 + 50.50 + 200.00 = 350.50
        assertEquals(0, new BigDecimal("350.50").compareTo(sum), "ผลรวมของบิลทั้งหมดในกลุ่มควรจะถูกต้อง");
    }

    @Test
    void findByGroupGroupIdAndPaidByUserUserId_shouldReturnBillsForSpecificUser() {
        // Act
        List<Bill> billsPaidByUser1 = billRepository.findByGroupGroupIdAndPaidByUserUserId(group1.getGroupId(), user1.getUserId());
        List<Bill> billsPaidByUser2 = billRepository.findByGroupGroupIdAndPaidByUserUserId(group1.getGroupId(), user2.getUserId());

        // Assert
        assertEquals(2, billsPaidByUser1.size(), "ควรจะเจอ 2 บิลที่จ่ายโดย user1");
        assertEquals(1, billsPaidByUser2.size(), "ควรจะเจอ 1 บิลที่จ่ายโดย user2");
        assertEquals("Tickets", billsPaidByUser2.get(0).getTitle());
    }
}