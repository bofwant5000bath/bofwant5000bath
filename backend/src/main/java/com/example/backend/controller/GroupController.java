package com.example.backend.controller;

import com.example.backend.dto.DashboardSummaryDto;
import com.example.backend.dto.GroupDetailsDto;
import com.example.backend.dto.CreateGroupRequest;
import com.example.backend.dto.UserDto;
import com.example.backend.model.Group;
import com.example.backend.model.GroupMember;
import com.example.backend.model.User;
import com.example.backend.repository.*;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final UserService userService;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public GroupController(UserService userService,
                           GroupRepository groupRepository,
                           GroupMemberRepository groupMemberRepository,
                           BillRepository billRepository,
                           PaymentRepository paymentRepository) {
        this.userService = userService;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(@PathVariable Integer userId) {
        List<Group> userGroups = groupMemberRepository.findGroupsByUserId(userId);

        BigDecimal totalOwed = BigDecimal.ZERO;
        BigDecimal totalReceivable = BigDecimal.ZERO;
        List<GroupDetailsDto> groupDetailsList = new ArrayList<>();

        for (Group group : userGroups) {
            Integer memberCount = groupMemberRepository.countByGroupId(group.getGroupId());
            BigDecimal groupTotalAmount = billRepository.sumAmountByGroupId(group.getGroupId());

            // ✅ คำนวณหนี้เราต้องจ่าย (expected - paid)
            BigDecimal myExpected = paymentRepository.sumExpectedForUser(userId, group.getGroupId());
            BigDecimal myPayments = paymentRepository.sumAmountByPayerUserIdAndGroupId(userId, group.getGroupId());
            BigDecimal myDebt = myExpected.subtract(myPayments);

            // ✅ คำนวณหนี้ที่เพื่อนติดเรา (expected - paid)
            BigDecimal expectedFromOthers = paymentRepository.sumExpectedFromOthers(userId, group.getGroupId());
            BigDecimal othersPaymentsToMe = paymentRepository.sumPaymentsFromOthersToMe(userId, group.getGroupId());
            BigDecimal othersDebtToMe = expectedFromOthers.subtract(othersPaymentsToMe);

            totalOwed = totalOwed.add(myDebt);
            totalReceivable = totalReceivable.add(othersDebtToMe);

            groupDetailsList.add(new GroupDetailsDto(group, memberCount, groupTotalAmount, myDebt, othersDebtToMe));
        }

        DashboardSummaryDto dashboardSummary = new DashboardSummaryDto(totalOwed, totalReceivable, groupDetailsList);
        return ResponseEntity.ok(dashboardSummary);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> allUsers = userService.getAllUsers();
        List<UserDto> userDtos = allUsers.stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @PostMapping("/create")
    public ResponseEntity<Group> createGroup(@RequestBody CreateGroupRequest request) {
        Optional<User> createdByUser = userService.findById(request.getCreatedByUserId());
        if (!createdByUser.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Group newGroup = new Group();
        newGroup.setGroupName(request.getGroupName());
        newGroup.setCreatedByUser(createdByUser.get());
        Group savedGroup = groupRepository.save(newGroup);

        for (Integer userId : request.getMemberIds()) {
            Optional<User> member = userService.findById(userId);
            if (member.isPresent()) {
                GroupMember groupMember = new GroupMember();
                groupMember.setGroup(savedGroup);
                groupMember.setUser(member.get());
                groupMemberRepository.save(groupMember);
            }
        }
        return new ResponseEntity<>(savedGroup, HttpStatus.CREATED);
    }
}
