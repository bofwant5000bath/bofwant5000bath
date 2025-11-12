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
import com.example.backend.model.*;
//import com.example.backend.model.BillParticipant; // ✅ เพิ่ม import
//import com.example.backend.model.Bill;
//import com.example.backend.model.Group;
//import com.example.backend.model.GroupMember;
//import com.example.backend.model.User;
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
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final UserService userService;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final BillParticipantRepository billParticipantRepository; // ✅ เพิ่ม Repository
    private final PinnedGroupRepository pinnedGroupRepository;

    @Autowired
    public GroupController(UserService userService,
                           GroupRepository groupRepository,
                           GroupMemberRepository groupMemberRepository,
                           BillRepository billRepository,
                           PaymentRepository paymentRepository,
                           BillParticipantRepository billParticipantRepository,
                           PinnedGroupRepository pinnedGroupRepository) { // ✅ เพิ่ม DI
        this.userService = userService;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.billParticipantRepository = billParticipantRepository; // ✅ เพิ่ม DI
        this.pinnedGroupRepository = pinnedGroupRepository;
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(@PathVariable Integer userId) {
        List<Group> userGroups = groupMemberRepository.findGroupsByUserId(userId);

        BigDecimal totalOwed = BigDecimal.ZERO;
        BigDecimal totalReceivable = BigDecimal.ZERO;
        List<GroupDetailsDto> groupDetailsList = new ArrayList<>();
        // ---- START: ดึงข้อมูลการปักหมุดมาก่อน ----
        // ดึงรายการที่ผู้ใช้คนนี้ปักหมุดไว้ทั้งหมด (แค่ครั้งเดียว)
        List<PinnedGroup> pinnedEntries = pinnedGroupRepository.findByIdUserId(userId);

        // แปลงเป็น Set ของ Group IDs เพื่อให้ค้นหาได้เร็ว (O(1))
        Set<Integer> pinnedGroupIds = pinnedEntries.stream()
                .map(pin -> pin.getId().getGroupId()) // ดึง groupId จาก EmbeddedId
                .collect(Collectors.toSet());
        // ---- END: ดึงข้อมูลการปักหมุด ----

        for (Group group : userGroups) {
            Integer memberCount = groupMemberRepository.countByGroupId(group.getGroupId());
            BigDecimal groupTotalAmount = billRepository.sumAmountByGroupId(group.getGroupId());

            // --- Logic การคำนวณ myDebt (เหมือนเดิมจากครั้งที่แล้ว) ---
            BigDecimal myDebtForGroup = BigDecimal.ZERO;
            List<BillParticipant> myParticipations = billParticipantRepository.findByUserUserIdAndBillGroupGroupId(userId, group.getGroupId());
            for (BillParticipant p : myParticipations) {
                BigDecimal amountPaidForBill = paymentRepository.sumAmountByPayerUserIdAndBillId(userId, p.getBill().getBillId());
                BigDecimal debtForThisBill = p.getSplitAmount().subtract(amountPaidForBill);
                if (debtForThisBill.compareTo(BigDecimal.ZERO) > 0) {
                    myDebtForGroup = myDebtForGroup.add(debtForThisBill);
                }
            }

            // ✅ ---- START: Logic การคำนวณ othersDebtToMe ใหม่ ----
            BigDecimal othersDebtToMeForGroup = BigDecimal.ZERO;

            // 1. ดึงบิลทั้งหมดในกลุ่มที่ "เรา" เป็นคนจ่าย
            List<Bill> billsPaidByMe = billRepository.findByGroupGroupIdAndPaidByUserUserId(group.getGroupId(), userId);

            for (Bill bill : billsPaidByMe) {
                // 2. สำหรับแต่ละบิล, ดูว่าใครเป็นหนี้เราบ้าง (participants)
                for (BillParticipant participant : bill.getParticipants()) { // ✅ ต้องมั่นใจว่า Bill entity มี @OneToMany participants
                    // 3. ไม่นับหนี้ของตัวเอง
                    if (participant.getUser().getUserId().equals(userId)) {
                        continue;
                    }

                    // 4. คำนวณว่าลูกหนี้คนนี้ จ่ายเงินสำหรับบิลนี้มาเท่าไหร่แล้ว
                    BigDecimal amountPaidByDebtor = paymentRepository.sumAmountByPayerUserIdAndBillId(
                            participant.getUser().getUserId(), bill.getBillId());

                    // 5. คำนวณหนี้ที่ยังค้างสำหรับส่วนนี้
                    BigDecimal remainingDebt = participant.getSplitAmount().subtract(amountPaidByDebtor);

                    // 6. ถ้ายอดค้างเป็นบวก (ยังติดหนี้) ถึงจะนำไปรวม
                    if (remainingDebt.compareTo(BigDecimal.ZERO) > 0) {
                        othersDebtToMeForGroup = othersDebtToMeForGroup.add(remainingDebt);
                    }
                }
            }
            // ✅ ---- END: Logic การคำนวณ othersDebtToMe ใหม่ ----


            totalOwed = totalOwed.add(myDebtForGroup);
            totalReceivable = totalReceivable.add(othersDebtToMeForGroup); // ⭐️ ใช้ค่าใหม่

            // ตรวจสอบว่ากลุ่มนี้อยู่ใน Set ที่เราดึงมาหรือไม่
            boolean isPinned = pinnedGroupIds.contains(group.getGroupId());

            groupDetailsList.add(new GroupDetailsDto(group, memberCount, groupTotalAmount, myDebtForGroup, othersDebtToMeForGroup, isPinned)); // ⭐️ ใช้ค่าใหม่
        }

        DashboardSummaryDto dashboardSummary = new DashboardSummaryDto(totalOwed, totalReceivable, groupDetailsList);
        return ResponseEntity.ok(dashboardSummary);
    }
    // ✅ 8. ---- START: เพิ่ม Endpoint ใหม่ทั้งหมด ----
    @GetMapping("/{groupId}/details")
    public ResponseEntity<GroupInfoDetailsDto> getGroupInfoDetails(
            @PathVariable Integer groupId,
            @RequestParam Integer userId) {

        // 1. ดึงข้อมูลกลุ่ม
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        if (!groupOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Group group = groupOptional.get();

        // 2. ดึงข้อมูลสมาชิกทั้งหมดในกลุ่ม
        List<GroupMember> members = groupMemberRepository.findByGroupGroupId(groupId);
        List<MemberDto> memberDtos = members.stream()
                .map(member -> new MemberDto(
                        member.getUser().getUserId(),
                        member.getUser().getFullName()
                ))
                .collect(Collectors.toList());

        // 3. ตรวจสอบสถานะการปักหมุด
        PinnedGroupId pinId = new PinnedGroupId(userId, groupId);
        boolean isPinned = pinnedGroupRepository.existsById(pinId);

        // 4. สร้าง DTO เพื่อส่งกลับ
        GroupInfoDetailsDto responseDto = new GroupInfoDetailsDto();
        responseDto.setGroupId(group.getGroupId());
        responseDto.setGroupName(group.getGroupName());
        responseDto.setPinned(isPinned);
        responseDto.setMembers(memberDtos);

        return ResponseEntity.ok(responseDto);
    }
    // ✅ 8. ---- END: เพิ่ม Endpoint ใหม่ทั้งหมด ----

    // ✅✅✅ ---- START: เพิ่ม Endpoint ใหม่ตรงนี้ ---- ✅✅✅
    @GetMapping("/all-with-members/{userId}")
    public ResponseEntity<List<GroupWithMembersDto>> getAllGroupsWithMembers(
            @PathVariable Integer userId) {

        // 1. ดึงกลุ่มทั้งหมดที่ User คนนี้อยู่ (เหมือน Dashboard)
        List<Group> userGroups = groupMemberRepository.findGroupsByUserId(userId);

        // 2. ดึงข้อมูลการปักหมุดของ User คนนี้ (เหมือน Dashboard)
        List<PinnedGroup> pinnedEntries = pinnedGroupRepository.findByIdUserId(userId);
        Set<Integer> pinnedGroupIds = pinnedEntries.stream()
                .map(pin -> pin.getId().getGroupId())
                .collect(Collectors.toSet());

        // 3. เตรียมลิสต์ Response ที่จะส่งกลับ
        List<GroupWithMembersDto> responseList = new ArrayList<>();

        // 4. วนลูปทุกกลุ่มที่ User อยู่
        for (Group group : userGroups) {

            // 5. ดึงข้อมูลสมาชิก *ทั้งหมด* ในกลุ่มนี้
            List<GroupMember> members = groupMemberRepository.findByGroupGroupId(group.getGroupId());
            List<MemberDto> memberDtos = members.stream()
                    .map(member -> new MemberDto(
                            member.getUser().getUserId(),
                            member.getUser().getFullName()
                    ))
                    .collect(Collectors.toList());

            // 6. ตรวจสอบสถานะการปักหมุด
            boolean isPinned = pinnedGroupIds.contains(group.getGroupId());

            // 7. สร้าง DTO ใหม่สำหรับกลุ่มนี้
            GroupWithMembersDto groupDto = new GroupWithMembersDto();
            groupDto.setGroupId(group.getGroupId());
            groupDto.setGroupName(group.getGroupName());
            groupDto.setPinned(isPinned);
            groupDto.setMembers(memberDtos);

            // 8. เพิ่มเข้าในลิสต์ที่จะส่งกลับ
            responseList.add(groupDto);
        }

        // 9. ส่งลิสต์ทั้งหมดกลับไป
        return ResponseEntity.ok(responseList);
    }
    // ✅✅✅ ---- END: Endpoint ใหม่ ---- ✅✅✅

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

        // 1. ตรวจสอบว่ากลุ่มนี้มีอยู่จริงหรือไม่
        Optional<Group> groupOptional = groupRepository.findById(groupId);
        if (!groupOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Group group = groupOptional.get();

        // 2. ดึงรายชื่อ ID สมาชิก *ที่มีอยู่เดิม* ทั้งหมดในกลุ่มนี้
        Set<Integer> existingMemberIds = groupMemberRepository.findByGroupGroupId(groupId)
                .stream()
                .map(groupMember -> groupMember.getUser().getUserId())
                .collect(Collectors.toSet());

        // 3. เตรียมลิสต์สำหรับเก็บสมาชิกที่ "เพิ่มใหม่จริงๆ" (เพื่อส่งกลับไปบอก Frontend)
        List<GroupMember> newMembersToSave = new ArrayList<>();

        // 4. วนลูป ID ที่ถูกส่งเข้ามา
        for (Integer userIdToAdd : request.getMemberIds()) {

            // ⭐️ นี่คือ Logic สำคัญ: ถ้า ID นี้ยังไม่มีในกลุ่ม (existingMemberIds)
            if (!existingMemberIds.contains(userIdToAdd)) {

                // 5. ตรวจสอบว่า User ID นี้มีตัวตนจริงหรือไม่
                Optional<User> userOptional = userService.findById(userIdToAdd);

                if (userOptional.isPresent()) {
                    // 6. สร้าง GroupMember ใหม่และเตรียมบันทึก
                    GroupMember newGroupMember = new GroupMember();
                    newGroupMember.setGroup(group);
                    newGroupMember.setUser(userOptional.get());
                    newMembersToSave.add(newGroupMember);
                }
                // (ถ้า userOptional.isPresent() == false ก็แค่ข้ามไป ไม่ทำอะไร)
            }
            // (ถ้า existingMemberIds.contains(userIdToAdd) == true ก็แค่ข้ามไป ไม่ทำอะไร)
        }

        // 7. บันทึกสมาชิกใหม่ทั้งหมดลง DB ในครั้งเดียว (ประสิทธิภาพดีกว่า save ทีละคน)
        if (!newMembersToSave.isEmpty()) {
            groupMemberRepository.saveAll(newMembersToSave);
        }

        // 8. แปลงร่าง newMembersToSave เป็น DTO เพื่อส่งกลับ
        List<MemberDto> addedMemberDtos = newMembersToSave.stream()
                .map(gm -> new MemberDto(gm.getUser().getUserId(), gm.getUser().getFullName()))
                .collect(Collectors.toList());

        // 9. ส่ง Status 201 Created พร้อมกับรายชื่อคนที่ถูกเพิ่มจริงๆ
        return ResponseEntity.status(HttpStatus.CREATED).body(addedMemberDtos);
    }
    
    // ✅ 6. เพิ่ม Endpoint ใหม่สำหรับ Pin/Unpin
    @PostMapping("/pin")
    public ResponseEntity<Void> togglePinGroup(@RequestBody PinGroupRequestDto request) {
        try {
            // สร้าง ID สำหรับค้นหา
            PinnedGroupId pinId = new PinnedGroupId(request.getUserId(), request.getGroupId());

            if (request.isPin()) {
                // --- Logic การ Pin ---
                // ตรวจสอบว่า User และ Group มีอยู่จริง
                Optional<User> user = userService.findById(request.getUserId());
                Optional<Group> group = groupRepository.findById(request.getGroupId());

                if (user.isPresent() && group.isPresent()) {
                    // สร้าง PinnedGroup ใหม่ (หรืออัปเดตถ้ามีอยู่แล้ว)
                    PinnedGroup pin = new PinnedGroup(user.get(), group.get());
                    pin.setPinned(true);
                    pinnedGroupRepository.save(pin);
                    return ResponseEntity.ok().build();
                } else {
                    // ถ้า User หรือ Group ไม่มีอยู่
                    return ResponseEntity.badRequest().build();
                }

            } else {
                // --- Logic การ Unpin ---
                // ตรวจสอบว่ามีข้อมูล Pin นี้อยู่หรือไม่
                Optional<PinnedGroup> existingPin = pinnedGroupRepository.findById(pinId);
                if (existingPin.isPresent()) {
                    // ถ้ามี ให้ลบออก
                    pinnedGroupRepository.delete(existingPin.get());
                }
                // ถ้าไม่มีอยู่แล้ว ก็ถือว่าสำเร็จ (Idempotent)
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
