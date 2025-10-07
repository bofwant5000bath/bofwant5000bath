package com.example.backend.controller;

import com.example.backend.dto.CreateBillRequest;
import com.example.backend.dto.CreateBillWithTagsRequest;
import com.example.backend.model.*;
import com.example.backend.model.SplitMethod;
import com.example.backend.repository.BillParticipantRepository;
import com.example.backend.service.BillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BillController.class)
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillService billService;

    @MockBean
    private BillParticipantRepository billParticipantRepository;

    private User mockUser;
    private Group mockGroup;
    private Bill mockBill;
    private BillParticipant mockParticipant;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");
        mockUser.setFullName("Test User");

        mockGroup = new Group();
        mockGroup.setGroupId(1);
        mockGroup.setGroupName("Test Group");

        mockBill = new Bill();
        mockBill.setBillId(101);
        mockBill.setGroup(mockGroup);
        mockBill.setTitle("Dinner");
        mockBill.setAmount(new BigDecimal("1000.00"));
        mockBill.setPaidByUser(mockUser);
        mockBill.setSplitMethod(SplitMethod.equal);
        mockBill.setBillDate(LocalDateTime.now());

        mockParticipant = new BillParticipant();
        mockParticipant.setBill(mockBill);
        mockParticipant.setUser(mockUser);
        mockParticipant.setSplitAmount(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("GET /group/{groupId} - Success: Should return list of bills for a group")
    void whenGetBillsByGroup_thenReturnListOfBillDetails() throws Exception {
        when(billService.getBillsByGroupId(anyInt())).thenReturn(Collections.singletonList(mockBill));
        when(billParticipantRepository.findByBillBillId(anyInt())).thenReturn(Collections.singletonList(mockParticipant));

        mockMvc.perform(get("/api/bills/group/{groupId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Dinner"))
                .andExpect(jsonPath("$[0].amount").value(1000.00))
                .andExpect(jsonPath("$[0].splitMethod").value("equal"))
                .andExpect(jsonPath("$[0].participants[0].user.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /create - Success: Should create a new bill and return its details")
    void whenCreateBill_withValidRequest_thenReturnsCreated() throws Exception {
        CreateBillRequest request = new CreateBillRequest();

        when(billService.createBill(any(CreateBillRequest.class))).thenReturn(mockBill);
        when(billParticipantRepository.findByBillBillId(mockBill.getBillId())).thenReturn(Collections.singletonList(mockParticipant));

        mockMvc.perform(post("/api/bills/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.billId").value(mockBill.getBillId()))
                .andExpect(jsonPath("$.title").value(mockBill.getTitle()));
    }

    @Test
    @DisplayName("POST /create - Failure: Should return Bad Request when service throws exception")
    void whenCreateBill_withInvalidData_thenReturnsBadRequest() throws Exception {
        CreateBillRequest request = new CreateBillRequest();
        when(billService.createBill(any(CreateBillRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid user or group ID"));

        mockMvc.perform(post("/api/bills/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /create/by-tag - Success: Should create a new bill by tag and return details")
    void whenCreateBillByTag_withValidRequest_thenReturnsCreated() throws Exception {
        CreateBillWithTagsRequest request = new CreateBillWithTagsRequest();

        Bill mockBillByTag = new Bill();
        mockBillByTag.setBillId(102);
        mockBillByTag.setTitle("Bill By Tag");
        mockBillByTag.setPaidByUser(mockUser);
        // ✅ แก้ไข: เพิ่มบรรทัดนี้เพื่อกำหนดค่า splitMethod
        mockBillByTag.setSplitMethod(SplitMethod.by_tag);

        when(billService.createBillByTag(any(CreateBillWithTagsRequest.class))).thenReturn(mockBillByTag);
        when(billParticipantRepository.findByBillBillId(mockBillByTag.getBillId())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/bills/create/by-tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Bill By Tag"))
                .andExpect(jsonPath("$.billId").value(102));
    }

    @Test
    @DisplayName("POST /create/by-tag - Failure: Should return Bad Request when service throws exception")
    void whenCreateBillByTag_withInvalidData_thenReturnsBadRequest() throws Exception {
        CreateBillWithTagsRequest request = new CreateBillWithTagsRequest();
        when(billService.createBillByTag(any(CreateBillWithTagsRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid tag details"));

        mockMvc.perform(post("/api/bills/create/by-tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}