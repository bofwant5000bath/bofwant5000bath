package com.example.backend.controller;

import com.example.backend.model.Bill;
import com.example.backend.model.BillParticipant;
import com.example.backend.model.User;
import com.example.backend.model.SplitMethod; // ✅ ต้องมี import นี้
import com.example.backend.repository.BillParticipantRepository;
import com.example.backend.service.BillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillController.class)
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @MockBean
    private BillService billService;

    @MockBean
    private BillParticipantRepository billParticipantRepository;

    @Test
    @DisplayName("GET /api/bills/group/{groupId} ควรคืนค่า 200 OK และ GroupBillDetailsDto")
    void getBillsByGroup_shouldReturn200_withGroupBillDetails() throws Exception {
        Integer groupId = 1;

        // --- Mock User ---
        User mockUser = new User();
        mockUser.setUserId(101);
        mockUser.setFullName("Test Member");
        when(billService.getGroupMembers(groupId)).thenReturn(List.of(mockUser));

        // --- Mock Bill ---
        Bill mockBill = new Bill();
        mockBill.setBillId(50);
        mockBill.setTitle("Test Bill");
        mockBill.setDescription("Test Bill Description");
        mockBill.setAmount(new BigDecimal("100.00"));
        mockBill.setPaidByUser(mockUser);

        // ✅ FIX: ใส่ exchangeRate และ currencyCode เพื่อป้องกัน NPE
        mockBill.setExchangeRate(new BigDecimal("1.00"));
        mockBill.setCurrencyCode("THB");
        mockBill.setSplitMethod(SplitMethod.equal); // ✅ ใช้ enum ที่มีอยู่จริง
        mockBill.setBillDate(LocalDateTime.now());

        when(billService.getBillsByGroupId(groupId)).thenReturn(List.of(mockBill));

        // --- Mock Participant ---
        BillParticipant mockParticipant = new BillParticipant();
        mockParticipant.setUser(mockUser);
        mockParticipant.setSplitAmount(new BigDecimal("50.00"));
        when(billParticipantRepository.findByBillBillId(50))
                .thenReturn(List.of(mockParticipant));

        // --- Act & Assert ---
        mockMvc.perform(get("/api/bills/group/" + groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupMembers[0].fullName").value("Test Member"))
                .andExpect(jsonPath("$.bills[0].description").value("Test Bill Description"))
                .andExpect(jsonPath("$.bills[0].amount").value(100.00));
    }
}
