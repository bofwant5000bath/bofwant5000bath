package com.example.backend.repository;

import com.example.backend.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BillParticipantRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private BillParticipantRepository billParticipantRepository;

    private User user1, user2;
    private Group group1;
    private Bill bill1, bill2;

    @BeforeEach
    void setup() {
        // Arrange: สร้างข้อมูลตั้งต้น
        user1 = new User(); user1.setUsername("u1"); user1.setPassword("p"); user1.setFullName("u1"); entityManager.persist(user1);
        user2 = new User(); user2.setUsername("u2"); user2.setPassword("p"); user2.setFullName("u2"); entityManager.persist(user2);
        group1 = new Group(); group1.setGroupName("G1"); entityManager.persist(group1);

        bill1 = new Bill(); bill1.setGroup(group1); bill1.setPaidByUser(user1); bill1.setAmount(new BigDecimal("100")); bill1.setTitle("B1"); bill1.setSplitMethod(SplitMethod.equal); bill1.setBillDate(LocalDateTime.now()); entityManager.persist(bill1);
        bill2 = new Bill(); bill2.setGroup(group1); bill2.setPaidByUser(user2); bill2.setAmount(new BigDecimal("50")); bill2.setTitle("B2"); bill2.setSplitMethod(SplitMethod.equal); bill2.setBillDate(LocalDateTime.now()); entityManager.persist(bill2);

        // Participants for Bill 1
        BillParticipant bp1_u1 = new BillParticipant();
        bp1_u1.setId(new BillParticipantId(bill1.getBillId(), user1.getUserId()));
        bp1_u1.setBill(bill1); bp1_u1.setUser(user1); bp1_u1.setSplitAmount(new BigDecimal("50"));
        bp1_u1.setIsPaid(PaymentStatus.unpaid); // ✅ เพิ่มบรรทัดนี้
        entityManager.persist(bp1_u1);

        BillParticipant bp1_u2 = new BillParticipant();
        bp1_u2.setId(new BillParticipantId(bill1.getBillId(), user2.getUserId()));
        bp1_u2.setBill(bill1); bp1_u2.setUser(user2); bp1_u2.setSplitAmount(new BigDecimal("50"));
        bp1_u2.setIsPaid(PaymentStatus.unpaid); // ✅ เพิ่มบรรทัดนี้
        entityManager.persist(bp1_u2);

        // Participant for Bill 2
        BillParticipant bp2_u1 = new BillParticipant();
        bp2_u1.setId(new BillParticipantId(bill2.getBillId(), user1.getUserId()));
        bp2_u1.setBill(bill2); bp2_u1.setUser(user1); bp2_u1.setSplitAmount(new BigDecimal("50"));
        bp2_u1.setIsPaid(PaymentStatus.unpaid); // ✅ เพิ่มบรรทัดนี้
        entityManager.persist(bp2_u1);

        entityManager.flush();
    }

    @Test
    void findByBillBillId_shouldReturnAllParticipantsForBill() {
        // Act
        List<BillParticipant> participants = billParticipantRepository.findByBillBillId(bill1.getBillId());

        // Assert
        assertEquals(2, participants.size(), "บิลที่ 1 ควรมีผู้เข้าร่วม 2 คน");
        assertTrue(participants.stream().anyMatch(p -> p.getUser().getUserId().equals(user1.getUserId())));
        assertTrue(participants.stream().anyMatch(p -> p.getUser().getUserId().equals(user2.getUserId())));
    }

    @Test
    void findByUserUserIdAndBillGroupGroupId_shouldReturnAllUserParticipationInGroup() {
        // Act
        List<BillParticipant> user1Participation = billParticipantRepository.findByUserUserIdAndBillGroupGroupId(user1.getUserId(), group1.getGroupId());
        List<BillParticipant> user2Participation = billParticipantRepository.findByUserUserIdAndBillGroupGroupId(user2.getUserId(), group1.getGroupId());

        // Assert
        assertEquals(2, user1Participation.size(), "User1 ควรมีส่วนร่วมใน 2 บิลของกลุ่มนี้");
        assertEquals(1, user2Participation.size(), "User2 ควรมีส่วนร่วมใน 1 บิลของกลุ่มนี้");
    }
}