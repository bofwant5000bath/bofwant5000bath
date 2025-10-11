package com.example.backend.controller;

import com.example.backend.dto.DashboardSummaryDto;
import com.example.backend.dto.GroupDetailsDto;
import com.example.backend.dto.CreateGroupRequest;
import com.example.backend.dto.UserDto;
import com.example.backend.model.BillParticipant; // ✅ เพิ่ม import
import com.example.backend.model.Bill;
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
    private final BillParticipantRepository billParticipantRepository; // ✅ เพิ่ม Repository

    @Autowired
    public GroupController(UserService userService,
                           GroupRepository groupRepository,
                           GroupMemberRepository groupMemberRepository,
                           BillRepository billRepository,
                           PaymentRepository paymentRepository,
                           BillParticipantRepository billParticipantRepository) { // ✅ เพิ่ม DI
        this.userService = userService;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.billParticipantRepository = billParticipantRepository; // ✅ เพิ่ม DI
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

            groupDetailsList.add(new GroupDetailsDto(group, memberCount, groupTotalAmount, myDebtForGroup, othersDebtToMeForGroup)); // ⭐️ ใช้ค่าใหม่
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
