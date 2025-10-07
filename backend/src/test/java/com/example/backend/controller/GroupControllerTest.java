package com.example.backend.controller;

import com.example.backend.dto.CreateGroupRequest;
import com.example.backend.model.Group;
import com.example.backend.model.User;
import com.example.backend.repository.BillRepository;
import com.example.backend.repository.GroupMemberRepository;
import com.example.backend.repository.GroupRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.service.UserService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
@DisplayName("GroupController Unit Tests")
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private GroupMemberRepository groupMemberRepository;

    @MockBean
    private BillRepository billRepository;

    @MockBean
    private PaymentRepository paymentRepository;

    private User testUser;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");

        testGroup = new Group();
        testGroup.setGroupId(101);
        testGroup.setGroupName("Test Group");
        testGroup.setCreatedByUser(testUser);
    }

    @Test
    @DisplayName("GET /dashboard/{userId} - Success Case")
    void whenGetDashboardSummary_withValidUserId_shouldReturnDashboardSummaryDto() throws Exception {
        // Arrange
        Integer userId = 1;

        when(groupMemberRepository.findGroupsByUserId(userId)).thenReturn(Collections.singletonList(testGroup));
        when(groupMemberRepository.countByGroupId(testGroup.getGroupId())).thenReturn(3);
        when(billRepository.sumAmountByGroupId(testGroup.getGroupId())).thenReturn(new BigDecimal("1500.00"));

        when(paymentRepository.sumExpectedForUser(eq(userId), eq(testGroup.getGroupId()))).thenReturn(new BigDecimal("500.00"));
        when(paymentRepository.sumAmountByPayerUserIdAndGroupId(eq(userId), eq(testGroup.getGroupId()))).thenReturn(new BigDecimal("200.00"));

        when(paymentRepository.sumExpectedFromOthers(eq(userId), eq(testGroup.getGroupId()))).thenReturn(new BigDecimal("1000.00"));
        when(paymentRepository.sumPaymentsFromOthersToMe(eq(userId), eq(testGroup.getGroupId()))).thenReturn(new BigDecimal("400.00"));

        // Act & Assert
        mockMvc.perform(get("/api/groups/dashboard/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOwed").value(300.00))
                .andExpect(jsonPath("$.totalReceivable").value(600.00))
                // ✅ **แก้ไขจุดนี้:** เปลี่ยนจาก groupDetailsList และโครงสร้างที่ซ้อนกัน เป็น "groups" และเข้าถึง property โดยตรง
                .andExpect(jsonPath("$.groups[0].groupName").value("Test Group"))
                .andExpect(jsonPath("$.groups[0].myDebt").value(300.00));
    }

    @Test
    @DisplayName("GET /users - Success Case")
    void whenGetAllUsers_shouldReturnListOfUsers() throws Exception {
        // Arrange
        User anotherUser = new User();
        anotherUser.setUserId(2);
        anotherUser.setUsername("anotheruser");
        anotherUser.setFullName("Another User");

        List<User> userList = List.of(testUser, anotherUser);
        when(userService.getAllUsers()).thenReturn(userList);

        // Act & Assert
        mockMvc.perform(get("/api/groups/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[1].username").value("anotheruser"));
    }

    @Test
    @DisplayName("POST /create - Success Case")
    void whenCreateGroup_withValidRequest_shouldReturnCreatedGroup() throws Exception {
        // Arrange
        CreateGroupRequest createGroupRequest = new CreateGroupRequest();
        createGroupRequest.setGroupName("New Test Group");
        createGroupRequest.setCreatedByUserId(1);
        createGroupRequest.setMemberIds(List.of(1, 2));

        User memberUser = new User();
        memberUser.setUserId(2);

        when(userService.findById(1)).thenReturn(Optional.of(testUser));
        when(userService.findById(2)).thenReturn(Optional.of(memberUser));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> {
            Group groupToSave = invocation.getArgument(0);
            groupToSave.setGroupId(102);
            groupToSave.setCreatedByUser(testUser);
            return groupToSave;
        });

        // Act & Assert
        mockMvc.perform(post("/api/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGroupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupId").exists())
                .andExpect(jsonPath("$.groupName").value("New Test Group"))
                .andExpect(jsonPath("$.createdByUser.userId").value(1));
    }
}