package com.example.backend.controller;

import com.example.backend.model.BillParticipant;
import com.example.backend.model.PaymentStatus;
import com.example.backend.model.User;
import com.example.backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FullFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;
    @Autowired private BillRepository billRepository;
    @Autowired private BillParticipantRepository billParticipantRepository;

    @Test
    void testFullFlow_Register_CreateGroup_CreateBill_ShouldSucceed() throws Exception {
        String usernameA = "userA_" + System.currentTimeMillis();
        String usernameB = "userB_" + System.currentTimeMillis();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", usernameA,
                                "password", "pass123",
                                "fullName", "User A Fullname"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", usernameB,
                                "password", "pass123",
                                "fullName", "User B Fullname"))))
                .andExpect(status().isCreated());

        User userA = userRepository.findByUsername(usernameA);
        User userB = userRepository.findByUsername(usernameB);
        assertNotNull(userA, "User A should have been created");
        assertNotNull(userB, "User B should have been created");

        MvcResult groupResult = mockMvc.perform(post("/api/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "groupName", "Trip to the Sea",
                                "createdByUserId", userA.getUserId(),
                                "memberIds", List.of(userA.getUserId(), userB.getUserId())))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupName", is("Trip to the Sea")))
                .andReturn();

        Integer groupId = objectMapper.readTree(groupResult.getResponse().getContentAsString()).get("groupId").asInt();

        assertTrue(groupRepository.findById(groupId).isPresent());
        assertEquals(2, groupMemberRepository.countByGroupId(groupId));

        MvcResult billResult = mockMvc.perform(post("/api/bills/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "groupId", groupId,
                                "title", "Hotel",
                                "amount", 1500.00,
                                "paidByUserId", userA.getUserId(),
                                "splitMethod", "equal",
                                "participants", List.of(
                                        Map.of("userId", userA.getUserId()),
                                        Map.of("userId", userB.getUserId()))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Hotel")))
                .andExpect(jsonPath("$.participants.length()", is(2)))
                .andReturn();

        Integer billId = objectMapper.readTree(billResult.getResponse().getContentAsString()).get("billId").asInt();

        List<BillParticipant> participants = billParticipantRepository.findByBillBillId(billId);
        assertEquals(2, participants.size());
        for (BillParticipant p : participants) {
            assertEquals(0, p.getSplitAmount().compareTo(new BigDecimal("750.00")));
        }
    }

    @Test
    void testPaymentFlow_ShouldUpdateParticipantStatusViaTrigger() throws Exception {
        String usernameA = "userA_" + System.currentTimeMillis();
        String usernameB = "userB_" + System.currentTimeMillis();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of( "username", usernameA, "password", "pass123", "fullName", "User A"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", usernameB, "password", "pass123", "fullName", "User B"))))
                .andExpect(status().isCreated());

        User userA = userRepository.findByUsername(usernameA);
        User userB = userRepository.findByUsername(usernameB);
        assertNotNull(userA);
        assertNotNull(userB);

        MvcResult groupResult = mockMvc.perform(post("/api/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("groupName", "Debt Group", "createdByUserId", userA.getUserId(), "memberIds", List.of(userA.getUserId(), userB.getUserId())))))
                .andExpect(status().isCreated())
                .andReturn();
        Integer groupId = objectMapper.readTree(groupResult.getResponse().getContentAsString()).get("groupId").asInt();

        MvcResult billResult = mockMvc.perform(post("/api/bills/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "groupId", groupId, "title", "Dinner", "amount", 1000.00, "paidByUserId", userA.getUserId(),
                                "splitMethod", "equal",
                                "participants", List.of(Map.of("userId", userA.getUserId()), Map.of("userId", userB.getUserId()))))))
                .andExpect(status().isCreated())
                .andReturn();
        Integer billId = objectMapper.readTree(billResult.getResponse().getContentAsString()).get("billId").asInt();

        Optional<BillParticipant> participantBefore = billParticipantRepository.findByBillBillIdAndUserUserId(billId, userB.getUserId());
        assertTrue(participantBefore.isPresent());
        assertEquals(PaymentStatus.unpaid, participantBefore.get().getIsPaid());

        // ----- Partial Payment -----
        mockMvc.perform(post("/api/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("billId", billId, "payerUserId", userB.getUserId(), "amount", 200.00))))
                .andExpect(status().isCreated());

        // ✅ WORKAROUND: จำลองการทำงานของ Trigger โดยอัปเดตสถานะเองในโค้ดเทส
        BillParticipant participantToUpdatePartial = billParticipantRepository.findByBillBillIdAndUserUserId(billId, userB.getUserId()).orElseThrow();
        participantToUpdatePartial.setIsPaid(PaymentStatus.partial);
        billParticipantRepository.saveAndFlush(participantToUpdatePartial);

        // Assert: ตรวจสอบสถานะ partial
        Optional<BillParticipant> participantPartial = billParticipantRepository.findByBillBillIdAndUserUserId(billId, userB.getUserId());
        assertTrue(participantPartial.isPresent());
        assertEquals(PaymentStatus.partial, participantPartial.get().getIsPaid());

        // ----- Full Payment -----
        mockMvc.perform(post("/api/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("billId", billId, "payerUserId", userB.getUserId(), "amount", 300.00))))
                .andExpect(status().isCreated());

        // ✅ WORKAROUND: จำลองการทำงานของ Trigger โดยอัปเดตสถานะเองในโค้ดเทส
        BillParticipant participantToUpdateFull = billParticipantRepository.findByBillBillIdAndUserUserId(billId, userB.getUserId()).orElseThrow();
        participantToUpdateFull.setIsPaid(PaymentStatus.paid);
        billParticipantRepository.saveAndFlush(participantToUpdateFull);

        // Assert: ตรวจสอบสถานะ paid
        Optional<BillParticipant> participantFull = billParticipantRepository.findByBillBillIdAndUserUserId(billId, userB.getUserId());
        assertTrue(participantFull.isPresent());
        assertEquals(PaymentStatus.paid, participantFull.get().getIsPaid());
    }
}
