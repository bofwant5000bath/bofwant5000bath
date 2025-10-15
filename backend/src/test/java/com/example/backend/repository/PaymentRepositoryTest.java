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
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    private User me, friend;
    private Group sharedGroup;
    private Bill billPaidByMe;

    @BeforeEach
    void setUp() {
        // Arrange: Create a comprehensive test scenario
        me = new User(); me.setUsername("me"); me.setPassword("p"); me.setFullName("Me");
        entityManager.persist(me);

        friend = new User(); friend.setUsername("friend"); friend.setPassword("p"); friend.setFullName("Friend");
        entityManager.persist(friend);

        sharedGroup = new Group(); sharedGroup.setGroupName("Dinner Group");
        entityManager.persist(sharedGroup);

        // A bill of 200, paid by "me", for a shared dinner
        billPaidByMe = new Bill();
        billPaidByMe.setGroup(sharedGroup);
        billPaidByMe.setPaidByUser(me);
        billPaidByMe.setAmount(new BigDecimal("200.00"));
        billPaidByMe.setTitle("Dinner");
        billPaidByMe.setSplitMethod(SplitMethod.equal);
        billPaidByMe.setBillDate(LocalDateTime.now());
        entityManager.persist(billPaidByMe);

        // My portion of the bill is 100, which I've effectively "paid" by fronting the money
        BillParticipant bpMe = new BillParticipant();
        bpMe.setId(new BillParticipantId(billPaidByMe.getBillId(), me.getUserId()));
        bpMe.setBill(billPaidByMe); bpMe.setUser(me);
        bpMe.setSplitAmount(new BigDecimal("100.00"));
        bpMe.setIsPaid(PaymentStatus.paid); // My part is considered paid
        entityManager.persist(bpMe);

        // Friend's portion is 100, which is currently unpaid
        BillParticipant bpFriend = new BillParticipant();
        bpFriend.setId(new BillParticipantId(billPaidByMe.getBillId(), friend.getUserId()));
        bpFriend.setBill(billPaidByMe); bpFriend.setUser(friend);
        bpFriend.setSplitAmount(new BigDecimal("100.00"));
        bpFriend.setIsPaid(PaymentStatus.unpaid);
        entityManager.persist(bpFriend);

        // The friend pays me back 70 out of the 100 they owe
        Payment paymentFromFriend = new Payment();
        paymentFromFriend.setBill(billPaidByMe);
        paymentFromFriend.setPayerUser(friend);
        paymentFromFriend.setAmount(new BigDecimal("70.00"));
        entityManager.persist(paymentFromFriend);

        entityManager.flush();
    }

    @Test
    void findByBillIdWithDetails_shouldReturnPaymentsForBill() {
        List<Payment> payments = paymentRepository.findByBillIdWithDetails(billPaidByMe.getBillId());
        assertEquals(1, payments.size());
        assertEquals(friend.getUsername(), payments.get(0).getPayerUser().getUsername());
    }

    @Test
    void sumAmountByPayerUserIdAndGroupId_shouldReturnTotalPaidByFriend() {
        // Total amount the friend has actually paid in this group
        BigDecimal sum = paymentRepository.sumAmountByPayerUserIdAndGroupId(friend.getUserId(), sharedGroup.getGroupId());
        assertEquals(0, new BigDecimal("70.00").compareTo(sum));
    }

    @Test
    void sumPaymentsFromOthersToMe_shouldReturnTotalReceivedByMe() {
        // Total amount I have received from others for bills I paid in this group
        BigDecimal sum = paymentRepository.sumPaymentsFromOthersToMe(me.getUserId(), sharedGroup.getGroupId());
        assertEquals(0, new BigDecimal("70.00").compareTo(sum));
    }

    @Test
    void sumExpectedForUser_shouldReturnTotalPortionForFriend() {
        // The friend's total share (what they are supposed to pay) across all bills in the group
        BigDecimal sum = paymentRepository.sumExpectedForUser(friend.getUserId(), sharedGroup.getGroupId());
        assertEquals(0, new BigDecimal("100.00").compareTo(sum));
    }

    @Test
    void sumExpectedFromOthers_shouldReturnTotalPortionOwedToMe() {
        // The total share others are supposed to pay me for bills I fronted
        BigDecimal sum = paymentRepository.sumExpectedFromOthers(me.getUserId(), sharedGroup.getGroupId());
        assertEquals(0, new BigDecimal("100.00").compareTo(sum));
    }
}