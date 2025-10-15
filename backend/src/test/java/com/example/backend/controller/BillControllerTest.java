package com.example.backend.controller;

import com.example.backend.dto.CreateBillRequest;
import com.example.backend.dto.CreateBillWithTagsRequest;
import com.example.backend.model.Bill;
import com.example.backend.model.BillParticipant;
import com.example.backend.model.SplitMethod;
import com.example.backend.model.User;
import com.example.backend.repository.BillParticipantRepository;
import com.example.backend.service.BillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillController.class)
public class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillService billService;

    @MockBean
    private BillParticipantRepository billParticipantRepository;

    private User user1, user2;
    private Bill bill1;
    private BillParticipant participant1, participant2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUserId(1);
        user1.setUsername("keatikun");

        user2 = new User();
        user2.setUserId(2);
        user2.setUsername("pornpawee");

        bill1 = new Bill();
        bill1.setBillId(1);
        bill1.setTitle("ค่าที่พัก");
        bill1.setAmount(new BigDecimal("4800.00"));
        bill1.setPaidByUser(user1);
        bill1.setSplitMethod(SplitMethod.equal);

        participant1 = new BillParticipant();
        participant1.setUser(user1);
        participant1.setSplitAmount(new BigDecimal("2400.00"));

        participant2 = new BillParticipant();
        participant2.setUser(user2);
        participant2.setSplitAmount(new BigDecimal("2400.00"));
    }

    @Test
    void whenGetBillsByGroup_withValidGroupId_shouldReturnGroupBillDetails() throws Exception {
        Integer groupId = 1;
        List<User> groupMembers = Arrays.asList(user1, user2);
        List<Bill> bills = Collections.singletonList(bill1);

        when(billService.getGroupMembers(groupId)).thenReturn(groupMembers);
        when(billService.getBillsByGroupId(groupId)).thenReturn(bills);

        mockMvc.perform(get("/api/bills/group/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupMembers", hasSize(2)))
                .andExpect(jsonPath("$.groupMembers[0].username", is("keatikun")))
                .andExpect(jsonPath("$.bills", hasSize(1)))
                .andExpect(jsonPath("$.bills[0].title", is("ค่าที่พัก")));
    }

    @Test
    void whenCreateBill_withValidRequest_shouldReturnCreatedBillDetails() throws Exception {
        CreateBillRequest request = new CreateBillRequest();
        request.setTitle("ค่าอาหารเย็น");
        request.setAmount(new BigDecimal("1500.00"));
        request.setSplitMethod(SplitMethod.equal);

        Bill createdBill = new Bill();
        createdBill.setBillId(2);
        createdBill.setTitle(request.getTitle());
        createdBill.setAmount(request.getAmount());
        createdBill.setPaidByUser(user1);
        createdBill.setSplitMethod(request.getSplitMethod());

        List<BillParticipant> participants = Arrays.asList(participant1, participant2);

        when(billService.createBill(any(CreateBillRequest.class))).thenReturn(createdBill);
        when(billParticipantRepository.findByBillBillId(createdBill.getBillId())).thenReturn(participants);

        mockMvc.perform(post("/api/bills/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                // **FIX:** Removed the incorrect ".bill" prefix
                .andExpect(jsonPath("$.title", is("ค่าอาหารเย็น")))
                .andExpect(jsonPath("$.participants", hasSize(2)));
    }

    @Test
    void whenCreateBillByTag_withValidRequest_shouldReturnCreatedBillDetails() throws Exception {
        CreateBillWithTagsRequest request = new CreateBillWithTagsRequest();
        request.setTitle("ค่าไฟฟ้า");
        request.setAmount(new BigDecimal("1200.00"));

        Bill createdBill = new Bill();
        createdBill.setBillId(3);
        createdBill.setTitle(request.getTitle());
        createdBill.setAmount(request.getAmount());
        createdBill.setPaidByUser(user1);
        createdBill.setSplitMethod(SplitMethod.by_tag);

        List<BillParticipant> participants = Arrays.asList(participant1, participant2);

        when(billService.createBillByTag(any(CreateBillWithTagsRequest.class))).thenReturn(createdBill);
        when(billParticipantRepository.findByBillBillId(createdBill.getBillId())).thenReturn(participants);

        mockMvc.perform(post("/api/bills/create/by-tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                // **FIX:** Removed the incorrect ".bill" prefix
                .andExpect(jsonPath("$.title", is("ค่าไฟฟ้า")))
                .andExpect(jsonPath("$.participants", hasSize(2)));
    }
}