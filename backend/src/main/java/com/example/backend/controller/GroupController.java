package com.example.backend.controller;

import com.example.backend.dto.DashboardSummaryDto;
import com.example.backend.dto.GroupDetailsDto;
import com.example.backend.dto.CreateGroupRequest;
import com.example.backend.dto.UserDto;
import com.example.backend.dto.PinGroupRequestDto;
import com.example.backend.dto.MemberDto;
import com.example.backend.dto.GroupInfoDetailsDto;
import com.example.backend.dto.GroupWithMembersDto;
import com.example.backend.dto.AddMembersRequestDto;
import com.example.backend.dto.SmartPaymentDto;
import com.example.backend.model.*;
import com.example.backend.repository.*;
import com.example.backend.service.UserService;
import com.example.backend.service.BillService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*") // 👈 เติมตรงนี้ครับ
public class GroupController {

    private final UserService userService;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final BillParticipantRepository billParticipantRepository; 
    private final PinnedGroupRepository pinnedGroupRepository;
    private final BillService billService;

    @Autowired
    public GroupController(UserService userService,
                           GroupRepository groupRepository,
                           GroupMemberRepository groupMemberRepository,
                           BillRepository billRepository,
                           PaymentRepository paymentRepository,
                           BillParticipantRepository billParticipantRepository,
                           PinnedGroupRepository pinnedGroupRepository,
                           BillService billService) { 
        this.userService = userService;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.billParticipantRepository = billParticipantRepository; 
        this.pinnedGroupRepository = pinnedGroupRepository;
        this.billService = billService;
    }

    // ... (Code ข้างล่างเหมือนเดิมเป๊ะ ผมละไว้เพื่อความสั้น แต่คุณไม่ต้องลบนะครับ) ...
    // ใส่ Code เดิมต่อจากนี้ได้เลยครับ
    
    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(@PathVariable Integer userId) {
       // ... โค้ดเดิม ...
       List<Group> userGroups = groupMemberRepository.findGroupsByUserId(userId);

        BigDecimal totalOwed = BigDecimal.ZERO;
        BigDecimal totalReceivable = BigDecimal.ZERO;
        List<GroupDetailsDto> groupDetailsList = new ArrayList<>();
        
        List<PinnedGroup> pinnedEntries = pinnedGroupRepository.findByIdUserId(userId);

        Set<Integer> pinnedGroupIds = pinnedEntries.stream()
                .map(pin -> pin.getId().getGroupId()) 
                .collect(Collectors.toSet());

        for (Group group : userGroups) {
            Integer memberCount = groupMemberRepository.countByGroupId(group.getGroupId());
            BigDecimal groupTotalAmount = billRepository.sumAmountByGroupId(group.getGroupId());

            BigDecimal myDebtForGroup = BigDecimal.ZERO;
            List<BillParticipant> myParticipations = billParticipantRepository.findByUserUserIdAndBillGroupGroupId(userId, group.getGroupId());
            for (BillParticipant p : myParticipations) {
                BigDecimal amountPaidForBill = paymentRepository.sumAmountByPayerUserIdAndBillId(userId, p.getBill().getBillId());
                BigDecimal debtForThisBill = p.getSplitAmount().subtract(amountPaidForBill);
                if (debtForThisBill.compareTo(BigDecimal.ZERO) > 0) {
                    myDebtForGroup = myDebtForGroup.add(debtForThisBill);
                }
            }

            BigDecimal othersDebtToMeForGroup = BigDecimal.ZERO;

            List<Bill> billsPaidByMe = billRepository.findByGroupGroupIdAndPaidByUserUserId(group.getGroupId(), userId);

            for (Bill bill : billsPaidByMe) {
                for (BillParticipant participant : bill.getParticipants()) { 
                    if (participant.getUser().getUserId().equals(userId)) {
                        continue;
                    }

                    BigDecimal amountPaidByDebtor = paymentRepository.sumAmountByPayerUserIdAndBillId(
                            participant.getUser().getUserId(), bill.getBillId());

                    BigDecimal remainingDebt = participant.getSplitAmount().subtract(amountPaidByDebtor);

                    if (remainingDebt.compareTo(BigDecimal.ZERO) > 0) {
                        othersDebtToMeForGroup = othersDebtToMeForGroup.add(remainingDebt);
                    }
                }
            }

            totalOwed = totalOwed.add(myDebtForGroup);
            totalReceivable = totalReceivable.add(othersDebtToMeForGroup); 

            boolean isPinned = pinnedGroupIds.contains(group.getGroupId());

            groupDetailsList.add(new GroupDetailsDto(group, memberCount, groupTotalAmount, myDebtForGroup, othersDebtToMeForGroup, isPinned)); 
        }

        DashboardSummaryDto dashboardSummary = new DashboardSummaryDto(totalOwed, totalReceivable, groupDetailsList);
        return ResponseEntity.ok(dashboardSummary);
    }
    
    @GetMapping("/{groupId}/details")
    public ResponseEntity<GroupInfoDetailsDto> getGroupInfoDetails(
            @PathVariable Integer groupId,
            @RequestParam Integer userId) {

        Optional<Group> groupOptional = groupRepository.findById(groupId);
        if (!groupOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Group group = groupOptional.get();

        List<GroupMember> members = groupMemberRepository.findByGroupGroupId(groupId);
        List<MemberDto> memberDtos = members.stream()
                .map(member -> new MemberDto(
                        member.getUser().getUserId(),
                        member.getUser().getFullName()
                ))
                .collect(Collectors.toList());

        PinnedGroupId pinId = new PinnedGroupId(userId, groupId);
        boolean isPinned = pinnedGroupRepository.existsById(pinId);

        GroupInfoDetailsDto responseDto = new GroupInfoDetailsDto();
        responseDto.setGroupId(group.getGroupId());
        responseDto.setGroupName(group.getGroupName());
        responseDto.setPinned(isPinned);
        responseDto.setMembers(memberDtos);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/all-with-members/{userId}")
    public ResponseEntity<List<GroupWithMembersDto>> getAllGroupsWithMembers(
            @PathVariable Integer userId) {

        List<Group> userGroups = groupMemberRepository.findGroupsByUserId(userId);

        List<PinnedGroup> pinnedEntries = pinnedGroupRepository.findByIdUserId(userId);
        Set<Integer> pinnedGroupIds = pinnedEntries.stream()
                .map(pin -> pin.getId().getGroupId())
                .collect(Collectors.toSet());

        List<GroupWithMembersDto> responseList = new ArrayList<>();

        for (Group group : userGroups) {

            List<GroupMember> members = groupMemberRepository.findByGroupGroupId(group.getGroupId());
            List<MemberDto> memberDtos = members.stream()
                    .map(member -> new MemberDto(
                            member.getUser().getUserId(),
                            member.getUser().getFullName()
                    ))
                    .collect(Collectors.toList());

            boolean isPinned = pinnedGroupIds.contains(group.getGroupId());

            GroupWithMembersDto groupDto = new GroupWithMembersDto();
            groupDto.setGroupId(group.getGroupId());
            groupDto.setGroupName(group.getGroupName());
            groupDto.setPinned(isPinned);
            groupDto.setMembers(memberDtos);

            responseList.add(groupDto);
        }

        return ResponseEntity.ok(responseList);
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

    @PostMapping("/{groupId}/members")
    public ResponseEntity<List<MemberDto>> addMembersToGroup(
            @PathVariable Integer groupId,
            @RequestBody AddMembersRequestDto request) {

        Optional<Group> groupOptional = groupRepository.findById(groupId);
        if (!groupOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Group group = groupOptional.get();

        Set<Integer> existingMemberIds = groupMemberRepository.findByGroupGroupId(groupId)
                .stream()
                .map(groupMember -> groupMember.getUser().getUserId())
                .collect(Collectors.toSet());

        List<GroupMember> newMembersToSave = new ArrayList<>();

        for (Integer userIdToAdd : request.getMemberIds()) {

            if (!existingMemberIds.contains(userIdToAdd)) {

                Optional<User> userOptional = userService.findById(userIdToAdd);

                if (userOptional.isPresent()) {
                    GroupMember newGroupMember = new GroupMember();
                    newGroupMember.setGroup(group);
                    newGroupMember.setUser(userOptional.get());
                    newMembersToSave.add(newGroupMember);
                }
            }
        }

        if (!newMembersToSave.isEmpty()) {
            groupMemberRepository.saveAll(newMembersToSave);
        }

        List<MemberDto> addedMemberDtos = newMembersToSave.stream()
                .map(gm -> new MemberDto(gm.getUser().getUserId(), gm.getUser().getFullName()))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(addedMemberDtos);
    }

    @PostMapping("/pin")
    public ResponseEntity<Void> togglePinGroup(@RequestBody PinGroupRequestDto request) {
        try {
            PinnedGroupId pinId = new PinnedGroupId(request.getUserId(), request.getGroupId());

            if (request.isPin()) {
                Optional<User> user = userService.findById(request.getUserId());
                Optional<Group> group = groupRepository.findById(request.getGroupId());

                if (user.isPresent() && group.isPresent()) {
                    PinnedGroup pin = new PinnedGroup(user.get(), group.get());
                    pin.setPinned(true);
                    pinnedGroupRepository.save(pin);
                    return ResponseEntity.ok().build();
                } else {
                    return ResponseEntity.badRequest().build();
                }

            } else {
                Optional<PinnedGroup> existingPin = pinnedGroupRepository.findById(pinId);
                if (existingPin.isPresent()) {
                    pinnedGroupRepository.delete(existingPin.get());
                }
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/{groupId}/settle")
    public ResponseEntity<List<SmartPaymentDto>> getSmartSettlementPlan(@PathVariable Integer groupId) {
        List<SmartPaymentDto> plan = billService.calculateSmartSettlement(groupId);
        return ResponseEntity.ok(plan);
    }
}
