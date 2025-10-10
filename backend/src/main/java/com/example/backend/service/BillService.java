package com.example.backend.service;

import com.example.backend.dto.CreateBillRequest;
import com.example.backend.dto.CreateBillWithTagsRequest;
import com.example.backend.model.*;
import com.example.backend.repository.BillParticipantRepository;
import com.example.backend.repository.BillRepository;
import com.example.backend.repository.GroupRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BillService {
    private final BillRepository billRepository;
    private final BillParticipantRepository billParticipantRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository; // ✅ new

    @Autowired
    public BillService(BillRepository billRepository,
                       BillParticipantRepository billParticipantRepository,
                       GroupRepository groupRepository,
                       UserRepository userRepository,
                       PaymentRepository paymentRepository) { // ✅ new
        this.billRepository = billRepository;
        this.billParticipantRepository = billParticipantRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository; // ✅ new
    }

    public List<Bill> getBillsByGroupId(Integer groupId) {
        return billRepository.findByGroupGroupId(groupId);
    }

    public List<User> getGroupMembers(Integer groupId) {
        return groupRepository.findUsersByGroupId(groupId);
    }

    @Transactional
    public Bill createBill(CreateBillRequest request) {
        Optional<Group> groupOptional = groupRepository.findById(request.getGroupId());
        Optional<User> paidByUserOptional = userRepository.findById(request.getPaidByUserId());

        if (groupOptional.isEmpty() || paidByUserOptional.isEmpty()) {
            throw new IllegalArgumentException("Group or user not found");
        }

        Bill newBill = new Bill();
        newBill.setGroup(groupOptional.get());
        newBill.setTitle(request.getTitle());
        newBill.setDescription(request.getDescription());
        newBill.setAmount(request.getAmount());
        newBill.setPaidByUser(paidByUserOptional.get());
        newBill.setSplitMethod(request.getSplitMethod());
        newBill.setBillDate(LocalDateTime.now());
        Bill savedBill = billRepository.save(newBill);

        // ✅ Equal Split
        if (newBill.getSplitMethod() == SplitMethod.equal) {
            List<User> participants;

            if (request.getSelectedParticipantIds() != null && !request.getSelectedParticipantIds().isEmpty()) {
                participants = userRepository.findAllById(request.getSelectedParticipantIds());
            } else {
                participants = groupRepository.findUsersByGroupId(request.getGroupId());
            }

            if (participants.isEmpty()) {
                throw new IllegalArgumentException("No participants found for equal split.");
            }

            BigDecimal equalSplitAmount = request.getAmount().divide(
                    BigDecimal.valueOf(participants.size()), 2, RoundingMode.HALF_UP);

            for (User member : participants) {
                BillParticipant billParticipant = new BillParticipant();
                billParticipant.setId(new BillParticipantId(savedBill.getBillId(), member.getUserId()));
                billParticipant.setBill(savedBill);
                billParticipant.setUser(member);
                billParticipant.setSplitAmount(equalSplitAmount);

                if (member.getUserId().equals(request.getPaidByUserId())) {
                    billParticipant.setIsPaid(PaymentStatus.paid);

                    // ✅ Insert Payment
                    Payment payment = new Payment();
                    payment.setBill(savedBill);
                    payment.setPayerUser(member);
                    payment.setAmount(equalSplitAmount);
                    paymentRepository.save(payment);

                } else {
                    billParticipant.setIsPaid(PaymentStatus.unpaid);
                }
                billParticipantRepository.save(billParticipant);
            }
        }

        // ✅ Unequal Split
        else if (newBill.getSplitMethod() == SplitMethod.unequal) {
            if (request.getParticipants() == null || request.getParticipants().isEmpty()) {
                throw new IllegalArgumentException("Participants list cannot be empty for unequal split.");
            }

            for (CreateBillRequest.ParticipantSplitDto participantDto : request.getParticipants()) {
                Optional<User> participantUser = userRepository.findById(participantDto.getUserId());
                if (participantUser.isPresent()) {
                    BillParticipant billParticipant = new BillParticipant();
                    billParticipant.setId(new BillParticipantId(savedBill.getBillId(), participantUser.get().getUserId()));
                    billParticipant.setBill(savedBill);
                    billParticipant.setUser(participantUser.get());
                    billParticipant.setSplitAmount(participantDto.getAmount());

                    if (participantDto.getUserId().equals(request.getPaidByUserId())) {
                        billParticipant.setIsPaid(PaymentStatus.paid);

                        // ✅ Insert Payment
                        Payment payment = new Payment();
                        payment.setBill(savedBill);
                        payment.setPayerUser(participantUser.get());
                        payment.setAmount(participantDto.getAmount());
                        paymentRepository.save(payment);

                    } else {
                        billParticipant.setIsPaid(PaymentStatus.unpaid);
                    }
                    billParticipantRepository.save(billParticipant);
                }
            }
        }

        return savedBill;
    }

    @Transactional
    public Bill createBillByTag(CreateBillWithTagsRequest request) {
        Optional<Group> groupOptional = groupRepository.findById(request.getGroupId());
        Optional<User> paidByUserOptional = userRepository.findById(request.getPaidByUserId());

        if (groupOptional.isEmpty() || paidByUserOptional.isEmpty()) {
            throw new IllegalArgumentException("Group or user not found");
        }

        Bill newBill = new Bill();
        newBill.setGroup(groupOptional.get());
        newBill.setTitle(request.getTitle());
        newBill.setDescription(request.getDescription());
        newBill.setAmount(request.getAmount());
        newBill.setPaidByUser(paidByUserOptional.get());
        newBill.setSplitMethod(request.getSplitMethod());
        newBill.setBillDate(LocalDateTime.now());
        Bill savedBill = billRepository.save(newBill);

        // ✅ รวมยอด splitAmount ต่อ user
        Map<Integer, BigDecimal> userAmountMap = new HashMap<>();
        for (CreateBillWithTagsRequest.TagSplitDto tagDto : request.getTags()) {
            for (CreateBillWithTagsRequest.ParticipantSplitDto participantDto : tagDto.getParticipants()) {
                userAmountMap.merge(
                        participantDto.getUserId(),
                        participantDto.getAmount(),
                        BigDecimal::add
                );
            }
        }

        // ✅ สร้าง BillParticipant + Payment (เฉพาะคนจ่าย)
        for (Map.Entry<Integer, BigDecimal> entry : userAmountMap.entrySet()) {
            Optional<User> participantUser = userRepository.findById(entry.getKey());
            if (participantUser.isPresent()) {
                BillParticipant billParticipant = new BillParticipant();
                billParticipant.setId(new BillParticipantId(savedBill.getBillId(), participantUser.get().getUserId()));
                billParticipant.setBill(savedBill);
                billParticipant.setUser(participantUser.get());
                billParticipant.setSplitAmount(entry.getValue());

                if (participantUser.get().getUserId().equals(request.getPaidByUserId())) {
                    billParticipant.setIsPaid(PaymentStatus.paid);

                    // ✅ Insert Payment
                    Payment payment = new Payment();
                    payment.setBill(savedBill);
                    payment.setPayerUser(participantUser.get());
                    payment.setAmount(entry.getValue());
                    paymentRepository.save(payment);

                } else {
                    billParticipant.setIsPaid(PaymentStatus.unpaid);
                }
                billParticipantRepository.save(billParticipant);
            }
        }

        return savedBill;
    }
}

