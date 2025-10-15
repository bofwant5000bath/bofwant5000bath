package com.example.backend.controller;

import com.example.backend.dto.CreateGroupRequest;
import com.example.backend.model.Bill;
import com.example.backend.model.BillParticipant;
import com.example.backend.model.Group;
import com.example.backend.model.User;
import com.example.backend.repository.*;
import com.example.backend.service.UserService;
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
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(GroupController.class)
public class GroupControllerTest {

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

    @MockBean
    private BillParticipantRepository billParticipantRepository;

    private User user1;
    private User user2;
    private Group group1;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUserId(1);
        user1.setUsername("keatikun");
        user1.setFullName("เกียรติกุล เข้มแข็ง");

        user2 = new User();
        user2.setUserId(2);
        user2.setUsername("pornpawee");
        user2.setFullName("พรพวีร์ พัฒนพรวิวัฒน์");

        group1 = new Group();
        group1.setGroupId(1);
        group1.setGroupName("ทริปเที่ยวทะเล");
        group1.setCreatedByUser(user1);
    }

    @Test
    void whenGetDashboardSummary_withValidUserId_shouldReturnDashboardSummaryDto() throws Exception {
        // Arrange
        Integer userId = 1;
        List<Group> userGroups = Collections.singletonList(group1);

        when(groupMemberRepository.findGroupsByUserId(userId)).thenReturn(userGroups);
        when(groupMemberRepository.countByGroupId(group1.getGroupId())).thenReturn(3);
        when(billRepository.sumAmountByGroupId(group1.getGroupId())).thenReturn(new BigDecimal("9300.00"));

        Bill billWhereIAmParticipant = new Bill();
        billWhereIAmParticipant.setBillId(101);

        BillParticipant myParticipation = new BillParticipant();
        myParticipation.setBill(billWhereIAmParticipant);
        myParticipation.setSplitAmount(new BigDecimal("2200.00"));

        when(billParticipantRepository.findByUserUserIdAndBillGroupGroupId(userId, group1.getGroupId()))
                .thenReturn(Collections.singletonList(myParticipation));
        when(paymentRepository.sumAmountByPayerUserIdAndBillId(userId, billWhereIAmParticipant.getBillId()))
                .thenReturn(BigDecimal.ZERO);

        Bill billPaidByMe = new Bill();
        billPaidByMe.setBillId(102);

        BillParticipant participantWhoOwesMe = new BillParticipant();
        participantWhoOwesMe.setUser(user2);
        participantWhoOwesMe.setSplitAmount(new BigDecimal("1600.00"));
        billPaidByMe.setParticipants(Collections.singletonList(participantWhoOwesMe));

        when(billRepository.findByGroupGroupIdAndPaidByUserUserId(group1.getGroupId(), userId))
                .thenReturn(Collections.singletonList(billPaidByMe));
        when(paymentRepository.sumAmountByPayerUserIdAndBillId(user2.getUserId(), billPaidByMe.getBillId()))
                .thenReturn(BigDecimal.ZERO);


        // Act & Assert
        mockMvc.perform(get("/api/groups/dashboard/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOwed", is(2200.0)))
                .andExpect(jsonPath("$.totalReceivable", is(1600.0)))
                // **FIX:** Changed "groupDetailsList" to "groups" to match actual JSON
                .andExpect(jsonPath("$.groups", hasSize(1)))
                // **FIX:** Changed "groupDetailsList" to "groups"
                .andExpect(jsonPath("$.groups[0].groupName", is("ทริปเที่ยวทะเล")))
                // **FIX:** Changed "groupDetailsList" to "groups"
                .andExpect(jsonPath("$.groups[0].memberCount", is(3)));
    }

    @Test
    void whenGetAllUsers_shouldReturnListOfUserDtos() throws Exception {
        // Arrange
        List<User> allUsers = Arrays.asList(user1, user2);
        when(userService.getAllUsers()).thenReturn(allUsers);

        // Act & Assert
        mockMvc.perform(get("/api/groups/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("keatikun")))
                .andExpect(jsonPath("$[1].username", is("pornpawee")));
    }

    @Test
    void whenCreateGroup_withValidRequest_shouldReturnCreatedGroup() throws Exception {
        // Arrange
        List<Integer> memberIds = Arrays.asList(1, 2);
        CreateGroupRequest request = new CreateGroupRequest();
        request.setGroupName("โปรเจคจบ");
        request.setCreatedByUserId(1);
        request.setMemberIds(memberIds);

        Group savedGroup = new Group();
        savedGroup.setGroupId(2);
        savedGroup.setGroupName(request.getGroupName());
        savedGroup.setCreatedByUser(user1);

        when(userService.findById(1)).thenReturn(Optional.of(user1));
        when(userService.findById(2)).thenReturn(Optional.of(user2));
        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

        // Act & Assert
        mockMvc.perform(post("/api/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupName", is("โปรเจคจบ")))
                .andExpect(jsonPath("$.createdByUser.userId", is(1)));
    }
}